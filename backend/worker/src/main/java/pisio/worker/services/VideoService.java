package pisio.worker.services;


import pisio.common.model.DTOs.Resolution;

import java.util.Optional;

public interface VideoService
{
    Optional<String> transcode(String inputFilePath, Resolution targetResolution, String processingID);
    Optional<String> extractAudio(String inputFilePath, String processingID);

}
