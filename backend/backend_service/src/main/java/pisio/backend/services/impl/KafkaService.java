package pisio.backend.services.impl;

import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pisio.backend.services.FilesService;
import pisio.common.model.DTOs.ProcessingItem;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;

@Service
@Slf4j
public class KafkaService
{
    private final FilesService filesService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public KafkaService(FilesService filesService, SimpMessagingTemplate simpMessagingTemplate)
    {
        this.filesService = filesService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @KafkaListener(topics = "${finished.topic-name}", groupId = "${kafka.listener.group-id}")
    public void listen(BaseMessage message)
    {
        log.info("Listener received a message: " + message);

        String presignedURL = filesService.createPresignURL(message.getBucket(), message.getFile(), 1, Method.GET);

        ProcessingItem item = ProcessingItem.builder()
                .itemID(null)
                .uploadTimestamp(null)
                .fileName(message.getFile())
                .progress(ProcessingProgress.FINISHED)
                .url(presignedURL)
                .build();

        simpMessagingTemplate.convertAndSend("/queue/notifications", item);
    }
}
