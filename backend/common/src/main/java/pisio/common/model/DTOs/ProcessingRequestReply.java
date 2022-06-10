package pisio.common.model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pisio.common.model.enums.ProcessingType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingRequestReply
{
    private String processingID;
    private String file;
    private ProcessingType operation;
}
