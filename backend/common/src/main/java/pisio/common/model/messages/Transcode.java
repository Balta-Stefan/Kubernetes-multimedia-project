package pisio.common.model.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pisio.common.model.enums.Resolutions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transcode extends BaseMessage
{
    private Resolutions targetResolution;
}
