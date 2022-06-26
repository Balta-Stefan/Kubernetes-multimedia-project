package pisio.worker.services;

import pisio.common.model.messages.BaseMessage;

public interface NotificationSender
{
    void sendNotification(BaseMessage msg);
}
