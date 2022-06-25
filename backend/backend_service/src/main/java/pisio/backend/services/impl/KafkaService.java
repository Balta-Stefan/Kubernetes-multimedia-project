package pisio.backend.services.impl;

import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.UserNotification;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;


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

    @KafkaListener(topics = "${kafka.topic.notifications}", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void listenFinishedTopic(BaseMessage notification)
    {
        log.info("Listener received a notification with file: " + notification.getFileName());
        UserNotification userNotification = new UserNotification(
                notification.getProcessingID(),
                notification.getFileName(),
                notification.getProgress(),
                null,
                notification.getType());

        if(notification.getProgress().equals(ProcessingProgress.FINISHED))
        {
            String presignedURL = filesService.createPresignURL(notification.getBucket(), notification.getObject(), objectExpiration, Method.GET);
            userNotification.setUrl(presignedURL);
        }
        else
        {
            userNotification.setProgress(ProcessingProgress.UNKNOWN);
        }

        simpMessagingTemplate.convertAndSend("/queue/" + notification.getMessageQueueID() + "/notifications", userNotification);
    }
}
