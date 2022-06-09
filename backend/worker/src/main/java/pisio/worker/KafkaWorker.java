package pisio.worker;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.common.model.messages.Transcode;
import pisio.worker.services.VideoService;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@KafkaListener(id = "${pending-topic-group-id}", topics = "${pending.topic-name}")
@Slf4j
@Service
public class KafkaWorker
{
    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;
    @Value("${finished.topic-name}")
    private String finishedTopicName;

    @Value("${prefix.finished}")
    private String finishedDirectoryPrefix;
    private final VideoService videoService;

    private final MinioClient minioClient;

    public KafkaWorker(KafkaTemplate<String, BaseMessage> kafkaTemplate, VideoService videoService, MinioClient minioClient)
    {
        this.kafkaTemplate = kafkaTemplate;
        this.videoService = videoService;
        this.minioClient = minioClient;
    }

    private Optional<String> downloadFile(String bucket, String object)
    {
        int dotIndex = object.lastIndexOf(".");
        String fileExtension = "";

        if(dotIndex != -1)
        {
            fileExtension = object.substring(dotIndex);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;
        log.info("Generated file name is: " + fileName);

        try
        {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .filename(fileName)
                            .build());

            return Optional.of(fileName);
        }
        catch(Exception e)
        {
            log.warn("An exception has occured in KafkaWorker.downloadFile");
            e.printStackTrace();
            return Optional.of(fileName);
        }
    }

    private boolean uploadFile(String bucket, String object, String filePath)
    {
        try
        {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket).object(object)
                            .filename(filePath)
                            .build());
        }
        catch(Exception e)
        {
            log.warn("Failed to upload a finished file");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    private void handleProcessedFile(BaseMessage msg, String processedObjectName, Optional<String> outputFilePathOpt)
    {
        BaseMessage response = new BaseMessage(msg);
        response.setProgress(ProcessingProgress.FAILED);
        response.setObject(null);

        String outputFilePath;
        if(outputFilePathOpt.isEmpty())
        {
            kafkaTemplate.send(finishedTopicName, response);
            log.warn("Media worker hasn't received output file of the ffmpeg class");
            return;
        }
        else
        {
            outputFilePath = outputFilePathOpt.get();
        }

        if(uploadFile(msg.getBucket(), processedObjectName, outputFilePath) == true)
        {
            response.setProgress(ProcessingProgress.FINISHED);
            response.setObject(processedObjectName);

            File file = new File(outputFilePath);
            if(file.delete() == false)
            {
                log.warn("Couldn't delete processed file: " + outputFilePath);
            }
        }

        kafkaTemplate.send(finishedTopicName, response);
        log.info("Worker has sent kafka message with progress: " + response.getProgress().name());
    }

    @KafkaHandler
    public void extractAudio(ExtractAudioMessage msg)
    {
        log.info("Worker has received an extract audio message with object" + msg.getObject() + " and type: " + msg.getType());
        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            String processedObjetName = finishedDirectoryPrefix + msg.getFileName() + "_AUDIO";
            handleProcessedFile(msg, processedObjetName, videoService.extractAudio(downloadedFilePath));
        });
    }

    @KafkaHandler
    public void transcodeVideo(Transcode msg)
    {
        log.info("Worker has received a transcode video message with file" + msg.getFileName() + " and type: " + msg.getType());
        log.info("Transcode resolution: " + msg.getTargetResolution());
        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            String processedObjetName = finishedDirectoryPrefix + msg.getFileName()
                    + "_TRANSCODED_"
                    + msg.getTargetResolution().getWidth()
                    + "x"
                    + msg.getTargetResolution().getHeight();
            handleProcessedFile(msg, processedObjetName, videoService.transcode(downloadedFilePath, msg.getTargetResolution()));
        });
    }
}
