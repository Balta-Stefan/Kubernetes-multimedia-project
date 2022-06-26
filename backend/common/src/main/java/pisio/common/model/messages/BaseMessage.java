package pisio.common.model.messages;

import lombok.*;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

import java.util.Objects;

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
    protected String processedObjectName;
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
        this.processedObjectName = msg.processedObjectName;
        this.fileName = msg.fileName;
        this.progress = msg.progress;
        this.type = msg.type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseMessage that = (BaseMessage) o;
        return processingID.equals(that.processingID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(processingID);
    }
}
