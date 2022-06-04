package pisio.common.model.DTOs;

import lombok.Builder;
import lombok.Data;
import pisio.common.model.enums.ProcessingProgress;

import java.time.LocalDateTime;

@Data
@Builder
public class ProcessingItem
{
    private Integer itemID;
    private LocalDateTime uploadTimestamp;
    private ProcessingProgress progress;
    private String fileName;
    private String url;
}
