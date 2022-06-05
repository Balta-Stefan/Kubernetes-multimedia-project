package pisio.backend.services.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.InternalServerError;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.ProcessingItem;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FilesServiceImpl implements FilesService
{
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.port}")
    private int minioEndpointPort;

    @Value("${minio.access_key}")
    private String accessKey;

    @Value("${minio.secret_access_key}")
    private String secret;

    @Value("${pending.topic-name}")
    private String pendingTopic;

    @Value("${finished.topic-name}")
    private String finishedTopic;

    private final String userBucketPrefix = "user-";
    private final String pendingDirectoryPrefix = "pending/";
    private final String finishedDirectoryPrefix = "finished/";

    private MinioClient minioClient;

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    public FilesServiceImpl(KafkaTemplate<String, BaseMessage> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }

    private String createUserBucketName(AuthenticatedUser user)
    {
        return userBucketPrefix + user.getUserID();
    }

    @PostConstruct
    void initMinioClient()
    {
        minioClient = MinioClient.builder()
                .endpoint(minioEndpoint, minioEndpointPort, false)
                .credentials(accessKey, secret)
                .build();
    }
    @Override
    public String createPresignURL(String bucket, String object, int expiryHours, Method method)
    {
        try
        {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(object)
                            .expiry(expiryHours, TimeUnit.HOURS)
                            .build());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new InternalServerError();
        }
    }

    @Override
    public List<String> requestPresignUrls(List<String> files, AuthenticatedUser user)
    {
        // Get presigned URL string to upload 'my-objectname' in 'my-bucketname'
        // with an expiration of 1 day.

        try
        {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(createUserBucketName(user)).build()) == false)
            {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(createUserBucketName(user)).build());
            }
        }
        catch(Exception e)
        {
            log.warn("Couldn't create a bucket: " + e.getMessage());
            e.printStackTrace();
            throw new InternalServerError();
        }

        try
        {
            List<String> presignedURLs = new ArrayList<>();
            for(String f : files)
            {
                String url = this.createPresignURL(
                        createUserBucketName(user),
                        pendingDirectoryPrefix + f,
                        1,
                        Method.PUT);
                presignedURLs.add(url);
            }

            return presignedURLs;
        }
        catch(Exception e)
        {
            log.warn("Presigning a bucket failed: " + e.getMessage());
            e.printStackTrace();
            throw new InternalServerError();
        }

    }

    @Override
    @SendToUser("/queue/notifications")
    public ProcessingItem uploadFinishedNotification(String file, AuthenticatedUser user)
    {
        ProcessingItem item = ProcessingItem.builder()
                .itemID(null)
                .uploadTimestamp(LocalDateTime.now())
                .fileName(file)
                .progress(ProcessingProgress.PENDING)
                .build();

        ExtractAudioMessage msg = new ExtractAudioMessage(
                user.getUsername(),
                createUserBucketName(user),
                pendingDirectoryPrefix,
                file);

        kafkaTemplate.send(pendingTopic, msg);

        return item;
    }

    private List<ProcessingItem> listBucketUtil(String bucket, String prefix, ProcessingProgress progress)
    {
        List<ProcessingItem> objects = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .build());

        results.forEach(obj ->
        {
            try
            {
                System.out.println(obj.get().objectName());
                String url = this.createPresignURL(
                        bucket,
                        obj.get().objectName(),
                        1,
                        Method.GET);

                ProcessingItem item = ProcessingItem.builder()
                        .itemID(null)
                        .fileName(obj.get().objectName())
                        .progress(progress)
                        .url(url)
                        .uploadTimestamp(obj.get().lastModified().toLocalDateTime())
                        .build();

                objects.add(item);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new InternalServerError();
            }
        });

        return objects;
    }

    @Override
    public List<ProcessingItem> listBucket(AuthenticatedUser user)
    {
        return Collections.emptyList();
        /*List<ProcessingItem> files = this.listBucketUtil(createUserBucketName(user), pendingDirectoryPrefix, ProcessingProgress.UNKNOWN);
        files.addAll(this.listBucketUtil(createUserBucketName(user), finishedDirectoryPrefix, ProcessingProgress.FINISHED));

        return files;*/
    }

    @Override
    public void deleteObject(String object, AuthenticatedUser user)
    {
        try
        {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(createUserBucketName(user))
                            .object(object).build());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            log.warn(e.getMessage());
        }
    }
}
