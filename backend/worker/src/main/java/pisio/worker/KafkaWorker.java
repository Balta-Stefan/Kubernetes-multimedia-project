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
        String fileName = UUID.randomUUID().toString();

        try
        {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .filename(fileName)
                            .build());
        }
        catch(Exception e)
        {
            log.warn("An exception has occured in KafkaWorker.downloadFile");
            e.printStackTrace();
            return Optional.of(fileName);
        }

        return Optional.empty();
    }

    private boolean uploadFile(String bucket, String fileName, String filePath)
    {
        try
        {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket).object("finished/" + fileName)
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



    private void handleProcessedFile(BaseMessage msg, Optional<String> outputFilePathOpt)
    {
        BaseMessage response = new BaseMessage(msg);
        response.setProgress(ProcessingProgress.FAILED);

        String outputFilePath;
        if(outputFilePathOpt.isEmpty())
        {
            kafkaTemplate.send(finishedTopicName, response);
            return;
        }
        else
        {
            outputFilePath = outputFilePathOpt.get();
        }

        if(uploadFile(msg.getBucket(), msg.getFileName(), outputFilePath) == true)
        {
            response.setProgress(ProcessingProgress.FINISHED);

            File file = new File(outputFilePath);
            if(file.delete() == false)
            {
                log.warn("Couldn't delete processed file: " + outputFilePath);
            }
        }
        else
        {
            log.warn("File not uploaded successfully.");
        }

        kafkaTemplate.send(finishedTopicName, response);
    }

    @KafkaHandler
    public void extractAudio(ExtractAudioMessage msg)
    {
        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            handleProcessedFile(msg, videoService.extractAudio(downloadedFilePath));
        });
    }

    @KafkaHandler
    public void transcodeVideo(Transcode msg)
    {
        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            handleProcessedFile(msg, videoService.transcode(downloadedFilePath, msg.getTargetResolution()));
        });
    }
}
