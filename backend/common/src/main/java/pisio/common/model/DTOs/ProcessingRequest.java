package pisio.common.model.DTOs;

import lombok.Data;

@Data
public class ProcessingRequest
{
    private boolean extractAudio;
    private Resolution targetResolution;
    private String file;
}
