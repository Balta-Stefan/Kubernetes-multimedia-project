package pisio.common.model.messages;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtractAudioMessage extends BaseMessage
{
    public ExtractAudioMessage(){}
    public ExtractAudioMessage(String username, String bucket, String prefix, String file)
    {
        super(username, bucket, prefix, file);
    }
}
