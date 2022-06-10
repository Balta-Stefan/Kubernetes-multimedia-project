package pisio.common.model.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pisio.common.model.DTOs.Resolution;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transcode extends BaseMessage
{
    private Resolution targetResolution;

    public Transcode(String processingID, Integer userID, String username, String bucket, String object, String fileName, ProcessingProgress progress, Resolution targetResolution)
    {
        super(processingID, userID, username, bucket, object, fileName, progress, ProcessingType.TRANSCODE);
        this.targetResolution = targetResolution;
    }

    public Transcode(BaseMessage msg)
    {
        super(msg);
    }
}
