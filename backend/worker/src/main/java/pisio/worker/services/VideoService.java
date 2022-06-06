package pisio.worker.services;

import pisio.common.model.enums.Resolutions;

import java.util.Optional;

public interface VideoService
{
    Optional<String> transcode(String inputFilePath, Resolutions targetResolution);
    Optional<String> extractAudio(String inputFilePath);

}
