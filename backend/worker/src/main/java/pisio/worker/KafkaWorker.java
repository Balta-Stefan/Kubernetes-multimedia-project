package pisio.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class KafkaWorker
{
    private Integer number = 0;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaWorker(KafkaTemplate<String, String> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void sendMessage()
    {
        log.warn("Producer is sending a message!");
        kafkaTemplate.send("my-topic", "Message number " + number.toString());
        number++;
    }

    @KafkaListener(topics = "my-topic", groupId = "grupa1")
    public void listen(String message)
    {
        log.warn("Listener received a message: " + message);
    }
}
