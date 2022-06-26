package pisio.worker.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.common.model.messages.Transcode;


@Slf4j
@Service
public class KafkaWorker
{
    private final MediaTaskExecutor mediaTaskExecutor;

    public KafkaWorker(MediaTaskExecutor taskExecutor)
    {
        this.mediaTaskExecutor = taskExecutor;
    }
    @KafkaListener(topics="${kafka.topic.canceled}", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void receiveCancelMessages(String processingID, Acknowledgment acknowledgment)
    {
        mediaTaskExecutor.stopProcess(processingID);
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics="${kafka.topic.pending}", groupId = "${pending-topic-group-id}")
    public void handlePendingMessage(@Payload(required = false) BaseMessage msg, Acknowledgment acknowledgment)
    {
        // return value specifies whether the message should be sent to the scheduler

        if(msg == null)
        {
            acknowledgment.acknowledge();
            return; // received a tombstone record
        }

        if(mediaTaskExecutor.checkCanProcessNewMessage() == false)
        {
            acknowledgment.nack(5000);
            return;
        }

        acknowledgment.acknowledge();

        switch(msg.getType())
        {
            case EXTRACT_AUDIO:
                mediaTaskExecutor.schedule((ExtractAudioMessage) msg);
                break;
            case TRANSCODE:
                mediaTaskExecutor.schedule((Transcode) msg);
                break;
            default:
                log.warn("Received unknown message.");
        }
    }
}
