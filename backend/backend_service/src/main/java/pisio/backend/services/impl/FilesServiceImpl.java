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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FilesServiceImpl implements FilesService
{
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access_key}")
    private String accessKey;

    @Value("${minio.secret_access_key}")
    private String secret;

    @Value("${pending.topic}")
    private String pendingTopic;

    @Value("${finished.topic}")
    private String finishedTopic;

    private final String userBucketPrefix = "user-";
    private final String pendingDirectoryPrefix = "pending/";
    private final String finishedDirectoryPrefix = "finished/";

    private MinioClient minioClient;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public FilesServiceImpl(KafkaTemplate<String, String> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostConstruct
    void initMinioClient()
    {
        minioClient = MinioClient.builder()
                .endpoint("localhost", 80, false)
                .credentials(accessKey, secret)
                .build();
    }

    private String createPresignURL(String bucket, String object, int expiry, Method method)
    {
        try
        {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(object)
                            .expiry(expiry, TimeUnit.HOURS)
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
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(userBucketPrefix + user.getUserID()).build()) == false)
            {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(userBucketPrefix + user.getUserID()).build());
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
                        userBucketPrefix + user.getUserID(),
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
        ProcessingItem item = ProcessingItem.builder().
                itemID(null)
                .uploadTimestamp(LocalDateTime.now())
                .fileName(file)
                .progress(ProcessingProgress.PENDING)
                .build();

        kafkaTemplate.send(pendingTopic, file);

        return item;
    }

    @Override
    public List<String> listBucket(AuthenticatedUser user)
    {
        List<String> objects = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(userBucketPrefix + user.getUserID())
                        .prefix(pendingDirectoryPrefix)
                        .build());

        results.forEach(obj ->
        {
            try
            {
                System.out.println(obj.get().objectName());
                String url = this.createPresignURL(
                        userBucketPrefix + user.getUserID(),
                        obj.get().objectName(),
                        1,
                        Method.GET);
                objects.add(url);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new InternalServerError();
            }
        });


        return objects;
    }
}
