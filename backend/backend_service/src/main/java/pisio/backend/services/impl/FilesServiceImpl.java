package pisio.backend.services.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.BadRequestException;
import pisio.backend.exceptions.InternalServerError;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.ProcessingRequest;
import pisio.common.model.DTOs.UserNotification;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.common.model.messages.Transcode;
import pisio.common.utils.BucketNameCreator;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FilesServiceImpl implements FilesService
{
    @Value("${pending.topic-name}")
    private String pendingTopic;

    @Value("${finished.topic-name}")
    private String finishedTopic;

    private final String userBucketPrefix = "user-";
    @Value("${prefix.pending}")
    private String pendingDirectoryPrefix;
    @Value("${prefix.finished}")
    private String finishedDirectoryPrefix;

    private final MinioClient minioClient;

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    public FilesServiceImpl(MinioClient minioClient, KafkaTemplate<String, BaseMessage> kafkaTemplate)
    {
        this.minioClient = minioClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    public String createPresignURL(String bucket, String object, int expiryHours, Method method)
    {
        try
        {
            Map<String, String> extraQueryParams = (method.equals(Method.GET) ?
                    Map.of("response-content-disposition", "attachment") : Collections.emptyMap());

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(method)
                            .bucket(bucket)
                            .object(object)
                            .extraQueryParams(extraQueryParams)
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
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(BucketNameCreator.createBucket(user.getUserID())).build()) == false)
            {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(BucketNameCreator.createBucket(user.getUserID())).build());
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
                        BucketNameCreator.createBucket(user.getUserID()),
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
    public void uploadFinishedNotification(ProcessingRequest request, AuthenticatedUser user)
    {
        int sentMessages = 0;

        BaseMessage baseMessage = new BaseMessage(user.getUserID(),
                user.getUsername(),
                BucketNameCreator.createBucket(user.getUserID()),
                pendingDirectoryPrefix + request.getFile(),
                request.getFile(),
                ProcessingProgress.PENDING,
                null);

        if(request.isExtractAudio())
        {
            baseMessage.setType(ProcessingType.EXTRACT_AUDIO);
            ExtractAudioMessage tempMsg = new ExtractAudioMessage(baseMessage);
            sentMessages++;
            log.info("Backend sending extract audio message with type: " + tempMsg.getType());
            kafkaTemplate.send(pendingTopic, tempMsg);
        }
        if(request.getTargetResolution().isValid())
        {
            baseMessage.setType(ProcessingType.TRANSCODE);
            Transcode tempMsg = new Transcode(baseMessage);
            tempMsg.setTargetResolution(request.getTargetResolution());
            log.info("Backend sending transcode message with type: " + tempMsg.getType());
            kafkaTemplate.send(pendingTopic, tempMsg);

            sentMessages++;
        }

        if(sentMessages == 0)
        {
            throw new BadRequestException();
        }

    }

    private List<UserNotification> listBucketUtil(String bucket, String prefix, ProcessingProgress progress)
    {
        List<UserNotification> objects = new ArrayList<>();
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
                String fileName = obj.get().objectName().replace(prefix, "");

                UserNotification notification = new UserNotification(fileName, progress, url, null);
                objects.add(notification);
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
    public List<UserNotification> listBucket(AuthenticatedUser user)
    {
        List<UserNotification> files = this.listBucketUtil(BucketNameCreator.createBucket(user.getUserID()), pendingDirectoryPrefix, ProcessingProgress.UNKNOWN);
        files.addAll(this.listBucketUtil(BucketNameCreator.createBucket(user.getUserID()), finishedDirectoryPrefix, ProcessingProgress.FINISHED));

        return files;
    }


    @Override
    public boolean deleteObject(String object, AuthenticatedUser user)
    {
        try
        {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BucketNameCreator.createBucket(user.getUserID()))
                            .object(object)
                            .build());
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            log.warn(e.getMessage());
            return false;
        }
    }

    @Override
    public void stopProcessing(String file, AuthenticatedUser user)
    {
        this.deleteObject(pendingDirectoryPrefix + file, user);
        if(this.deleteObject(finishedDirectoryPrefix + file, user) == false)
        {
            // the object hasn't been processed, send a delete message

        }

    }
}
