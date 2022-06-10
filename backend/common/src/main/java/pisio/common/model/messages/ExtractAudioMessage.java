package pisio.common.model.messages;


import lombok.Data;
import lombok.EqualsAndHashCode;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtractAudioMessage extends BaseMessage
{
    public ExtractAudioMessage(){}
    public ExtractAudioMessage(String processingID, Integer userID, String username, String bucket, String object, String fileName, ProcessingProgress progress)
    {
        super(processingID, userID, username, bucket, object, fileName, progress, ProcessingType.EXTRACT_AUDIO);
    }

    public ExtractAudioMessage(BaseMessage msg)
    {
        super(msg);
    }
}
