package pisio.backend.services.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.BadRequestException;
import pisio.backend.exceptions.InternalServerError;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.models.DTOs.PresignedUploadLink;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.ProcessingItem;
import pisio.common.model.DTOs.ProcessingRequest;
import pisio.common.model.DTOs.ProcessingRequestReply;
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
    @Value("${kafka.topic.pending}")
    private String pendingTopic;

    @Value("${kafka.topic.canceled}")
    private String canceledTopic;

    @Value("${prefix.pending}")
    private String pendingDirectoryPrefix;
    @Value("${prefix.finished}")
    private String finishedDirectoryPrefix;

    @Value("${minio.presigned-url-expiration-hours}")
    private Integer objectExpiration;

    private final MinioClient minioClient;

    private final KafkaTemplate<String, BaseMessage> pendingTopicKafkaTemplate;
    private final KafkaTemplate<String, String> canceledTopicKafkaTemplate;

    public FilesServiceImpl(MinioClient minioClient, KafkaTemplate<String, BaseMessage> pendingTopicKafkaTemplate, KafkaTemplate<String, String> canceledTopicKafkaTemplate)
    {
        this.minioClient = minioClient;
        this.pendingTopicKafkaTemplate = pendingTopicKafkaTemplate;
        this.canceledTopicKafkaTemplate = canceledTopicKafkaTemplate;
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
    public List<PresignedUploadLink> requestPresignUrls(List<String> files, AuthenticatedUser user)
    {
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
            List<PresignedUploadLink> presignedURLs = new ArrayList<>();
            for(String f : files)
            {
                String url = this.createPresignURL(
                        BucketNameCreator.createBucket(user.getUserID()),
                        pendingDirectoryPrefix + f,
                        objectExpiration,
                        Method.PUT);
                presignedURLs.add(new PresignedUploadLink(f, url));
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
    public List<ProcessingRequestReply> uploadFinishedNotification(ProcessingRequest request, AuthenticatedUser user)
    {
        int sentMessages = 0;

        BaseMessage baseMessage = new BaseMessage(
                user.getMessageQueueID(),
                null,
                user.getUserID(),
                user.getUsername(),
                BucketNameCreator.createBucket(user.getUserID()),
                pendingDirectoryPrefix + request.getFile(),
                request.getFile(),
                ProcessingProgress.PENDING,
                null);

        List<ProcessingRequestReply> replies = new ArrayList<>();

        if(request.isExtractAudio())
        {
            ExtractAudioMessage tempMsg = new ExtractAudioMessage(baseMessage);
            tempMsg.setType(ProcessingType.EXTRACT_AUDIO);
            tempMsg.setProcessingID(UUID.randomUUID().toString());
            sentMessages++;
            log.info("Backend sending extract audio message with type: " + tempMsg.getType());
            pendingTopicKafkaTemplate.send(pendingTopic, tempMsg.getProcessingID(), tempMsg);

            replies.add(new ProcessingRequestReply(tempMsg.getProcessingID(), tempMsg.getFileName(), tempMsg.getType()));
        }
        if(request.getTargetResolution().isValid())
        {
            Transcode tempMsg = new Transcode(baseMessage);
            tempMsg.setType(ProcessingType.TRANSCODE);
            tempMsg.setProcessingID(UUID.randomUUID().toString());
            tempMsg.setTargetResolution(request.getTargetResolution());
            log.info("Backend sending transcode message with type: " + tempMsg.getType());
            pendingTopicKafkaTemplate.send(pendingTopic, tempMsg.getProcessingID(), tempMsg);

            replies.add(new ProcessingRequestReply(tempMsg.getProcessingID(), tempMsg.getFileName(), tempMsg.getType()));

            sentMessages++;
        }

        if(sentMessages == 0)
        {
            throw new BadRequestException();
        }

        return replies;
    }

    private List<ProcessingItem> listBucketUtil(String bucket, String prefix, ProcessingProgress progress, boolean fromPendingDirectory)
    {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build());

        Map<String, ProcessingItem> items = new HashMap<>();

        results.forEach(obj ->
        {
            try
            {
                String objectName = obj.get().objectName();
                String url = this.createPresignURL(bucket, objectName, objectExpiration, Method.GET);
                String fileName;
                ProcessingType type = null;

                if(fromPendingDirectory == true)
                {
                    fileName = objectName.replace(pendingDirectoryPrefix, "");
                }
                else
                {
                    String fullPath = objectName.substring(finishedDirectoryPrefix.length()); // take everything behind finished/
                    String processedFileName = fullPath.substring(fullPath.lastIndexOf("/")+1);
                    fileName = fullPath.substring(0, fullPath.indexOf("/"));
                    if(processedFileName.contains("AUDIO")) // object metadata is always null for some reason so this method has to be used...
                    {
                        type = ProcessingType.EXTRACT_AUDIO;
                    }
                    else
                    {
                        type = ProcessingType.TRANSCODE;
                    }
                }

                ProcessingItem tempItem = items.get(fileName);
                UserNotification tempNotification = new UserNotification(null, fileName, progress, url, type);

                if(tempItem != null)
                {
                    tempItem.getNotifications().add(tempNotification);
                }
                else
                {
                    tempItem = new ProcessingItem(fileName, new ArrayList<>());
                    tempItem.getNotifications().add(tempNotification);
                    items.put(fileName, tempItem);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        });

        return new ArrayList<>(items.values());
    }

    @Override
    public List<ProcessingItem> listBucket(AuthenticatedUser user)
    {
        String userBucket = BucketNameCreator.createBucket(user.getUserID());
        List<ProcessingItem> files = this.listBucketUtil(userBucket, pendingDirectoryPrefix, ProcessingProgress.UNKNOWN, true);
        files.addAll(this.listBucketUtil(userBucket, finishedDirectoryPrefix, ProcessingProgress.FINISHED, false));

        return files;
    }

    private boolean deleteObjectUtil(String bucket, String object)
    {
        try
        {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .build());
            return true;
        }
        catch (Exception e)
        {
            log.warn("Couldn't delete object: " + object + ", in bucket: " + bucket);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteObject(String bucket, String object, boolean recursive)
    {
        if(recursive)
        {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .recursive(true)
                            .build());

            for(Result<Item> res : results)
            {
                try
                {
                    this.deleteObjectUtil(bucket, res.get().objectName());
                }
                catch(Exception e)
                {
                    log.warn("Couldn't retrieve object recursively from bucket: " + bucket + ", and directory: " + object);
                }
            }
            return true;
        }
        else
        {
            return this.deleteObjectUtil(bucket, object);
        }
    }

    @Override
    public boolean deletePendingObject(String file, AuthenticatedUser user)
    {
        String bucket = BucketNameCreator.createBucket(user.getUserID());
        String object = pendingDirectoryPrefix + file;

        return deleteObjectUtil(bucket, object);
    }

    @Override
    public void stopProcessing(String file, String processingID, AuthenticatedUser user)
    {
        String bucket = BucketNameCreator.createBucket(user.getUserID());
        this.deleteObject(bucket, pendingDirectoryPrefix + file, false);
        log.info("Deleted pending file: " + file + ", in bucket: " + bucket + ", with processingID: " + processingID);
        pendingTopicKafkaTemplate.send(pendingTopic, processingID, null);
        canceledTopicKafkaTemplate.send(canceledTopic, processingID);

        this.deleteObject(BucketNameCreator.createBucket(user.getUserID()), finishedDirectoryPrefix + file, true);
    }
}
