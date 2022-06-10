package pisio.common.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseMessage
{
    protected String messageQueueID;
    protected String processingID;
    protected Integer userID;
    protected String username; // username is needed when sending a message to the user's queue
    protected String bucket;
    protected String object;
    protected String fileName;
    protected ProcessingProgress progress;
    protected ProcessingType type;

    public BaseMessage(BaseMessage msg)
    {
        this.messageQueueID = msg.messageQueueID;
        this.processingID = msg.processingID;
        this.userID = msg.userID;
        this.username = msg.username;
        this.bucket = msg.bucket;
        this.object = msg.object;
        this.fileName = msg.fileName;
        this.progress = msg.progress;
        this.type = msg.type;
    }
}
