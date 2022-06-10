package pisio.backend.services.impl;

import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.UserNotification;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;
import pisio.common.utils.BucketNameCreator;

import java.util.Collections;

@Service
@Slf4j
public class KafkaService
{
    private final FilesService filesService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Value("${prefix.pending}")
    private String pendingDirectoryPrefix;

    @Value("${minio.presigned-url-expiration-hours}")
    private Integer objectExpiration;

    public KafkaService(FilesService filesService, SimpMessagingTemplate simpMessagingTemplate)
    {
        this.filesService = filesService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @KafkaListener(topics="${kafka.topic.processing}", groupId = "${kafka.listener.group-id}")
    public void listenProcessingTopic(BaseMessage notification)
    {
        log.info("Listener received a processing notification with file: " + notification.getFileName());
        UserNotification userNotification = new UserNotification(
                notification.getProcessingID(),
                notification.getFileName(),
                notification.getProgress(),
                null,
                notification.getType());

        simpMessagingTemplate.convertAndSend("/topic/notifications", userNotification);
    }

    @KafkaListener(topics = "${kafka.topic.finished}", groupId = "${kafka.listener.group-id}")
    public void listenFinishedTopic(BaseMessage notification)
    {
        log.info("Listener received a notification with file: " + notification.getFileName());
        UserNotification userNotification = new UserNotification(
                notification.getProcessingID(),
                notification.getFileName(),
                notification.getProgress(),
                null,
                notification.getType());

        if(notification.getProgress().equals(ProcessingProgress.FAILED))
        {
            log.warn("Processing failed");
        }
        else if(notification.getProgress().equals(ProcessingProgress.FINISHED))
        {
            String presignedURL = filesService.createPresignURL(notification.getBucket(), notification.getObject(), objectExpiration, Method.GET);
            userNotification.setUrl(presignedURL);
        }
        else
        {
            userNotification.setProgress(ProcessingProgress.UNKNOWN);
        }
        // remove the object from the pending directory
        filesService.deleteObject(BucketNameCreator.createBucket(notification.getUserID()), pendingDirectoryPrefix + notification.getFileName(), false);

        simpMessagingTemplate.convertAndSend("/topic/notifications", userNotification);
    }
}
