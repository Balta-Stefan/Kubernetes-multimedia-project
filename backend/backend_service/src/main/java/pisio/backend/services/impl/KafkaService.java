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

    @Value("${minio.presigned-url-expiration-hours}")
    private Integer objectExpiration;

    public KafkaService(FilesService filesService, SimpMessagingTemplate simpMessagingTemplate)
    {
        this.filesService = filesService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @KafkaListener(topics = "${finished.topic-name}", groupId = "${kafka.listener.group-id}")
    public void listen(BaseMessage notification)
    {
        log.info("Listener received a notification: ");
        UserNotification userNotification = new UserNotification(notification.getFileName(), notification.getProgress(), null);

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

        simpMessagingTemplate.convertAndSend("/queue/notifications", userNotification);
    }
}
