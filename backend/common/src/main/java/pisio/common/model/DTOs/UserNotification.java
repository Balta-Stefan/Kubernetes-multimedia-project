package pisio.common.model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNotification
{
    private String fileName;
    private ProcessingProgress progress;
    private String url;
    private ProcessingType type;
}
