package pisio.common.model.messages;


import lombok.Data;
import lombok.EqualsAndHashCode;
import pisio.common.model.enums.ProcessingProgress;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtractAudioMessage extends BaseMessage
{
    public ExtractAudioMessage(){}
    public ExtractAudioMessage(Integer userID, String username, String bucket, String object, String fileName, ProcessingProgress progress)
    {
        super(userID, username, bucket, object, fileName, progress);
    }

    public ExtractAudioMessage(BaseMessage msg)
    {
        super(msg);
    }
}
