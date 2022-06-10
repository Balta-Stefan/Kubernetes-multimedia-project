package pisio.common.model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.enums.ProcessingType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNotification
{
    @Nullable
    private String processingID;
    private String fileName;
    private ProcessingProgress progress;
    @Nullable
    private String url;
    private ProcessingType type;
}
