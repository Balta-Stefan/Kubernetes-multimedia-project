package pisio.worker.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pisio.common.model.DTOs.Resolution;
import pisio.worker.services.VideoService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService
{
    private final CancelProcessingService cancelProcessingService;

    public VideoServiceImpl(CancelProcessingService cancelProcessingService)
    {
        this.cancelProcessingService = cancelProcessingService;
    }

    private Optional<String> process(List<String> command, String outputFilePath, String processingID)
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.inheritIO();

        log.info("Video processor has received command: " + command);

        try
        {
            Process process = pb.start();
            cancelProcessingService.registerProcess(processingID, process);
            process.onExit().get();

            if(process.exitValue() != 0)
            {
                log.warn("Video processor exit code is not 0");

                File file = new File(outputFilePath);
                if(file.exists() && file.delete() == false)
                {
                    log.warn("Couldn't delete failed output file: " + outputFilePath);
                }
            }
            else
            {
                log.info("VideoService is outputting to file: " + outputFilePath);
                return Optional.of(outputFilePath);
            }
        }
        catch (InterruptedException | IOException | ExecutionException e)
        {
            log.warn("An exception has occured while trying to process a video:" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            cancelProcessingService.deregisterProcess(processingID);
        }
        return Optional.empty();
    }

    private String changeFileExtension(String filePath, String extension)
    {
        int dotIndex = filePath.lastIndexOf(".");
        String outputPath;

        if(dotIndex != -1)
        {
            outputPath = filePath.substring(0, dotIndex) + "." + extension;
        }
        else
        {
            outputPath = filePath + "." + extension;
        }

        return outputPath;
    }
    @Override
    public Optional<String> transcode(String inputFilePath, Resolution targetResolution, String processingID)
    {
        String outputPath = UUID.randomUUID() + ".mp4";//"OUT_" + changeFileExtension(inputFilePath, "mp4");

        List<String> command = List.of("ffmpeg",
                "-i",
                inputFilePath,
                "-vf",
                "scale=" + targetResolution.getWidth() + ":" + targetResolution.getHeight(),
                "-c:a",
                "copy",
                outputPath);

        return process(command, outputPath, processingID);
    }

    @Override
    public Optional<String> extractAudio(String inputFilePath, String processingID)
    {
        // ffmpeg -i inputFilePath -map 0:a -c copy outputPath
        String outputPath = UUID.randomUUID() + ".mp4";//changeFileExtension(inputFilePath, "mp4");

        return process(List.of("ffmpeg", "-i", inputFilePath, "-map", "0:a", "-c", "copy", outputPath), outputPath, processingID);
    }
}
