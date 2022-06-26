package pisio.worker.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pisio.common.model.messages.BaseMessage;
import pisio.worker.services.NotificationSender;

@Service
public class KafkaNotificationSender implements NotificationSender
{
    @Value("${kafka.topic.notifications}")
    private String notificationsTopicName;
    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    public KafkaNotificationSender(KafkaTemplate<String, BaseMessage> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendNotification(BaseMessage notification)
    {
        kafkaTemplate.send(notificationsTopicName, notification);
    }
}
