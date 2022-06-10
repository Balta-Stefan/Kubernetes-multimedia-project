package pisio.worker;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.common.model.messages.Transcode;
import pisio.worker.services.VideoService;
import pisio.worker.services.impl.CancelProcessingService;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class KafkaWorker
{
    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;
    @Value("${kafka.topic.finished}")
    private String finishedTopicName;

    @Value("${kafka.topic.canceled}")
    private String canceledTopic;

    @Value("${kafka.topic.processing}")
    private String processingTopicName;

    @Value("${prefix.finished}")
    private String finishedDirectoryPrefix;
    private final VideoService videoService;
    private final CancelProcessingService cancelProcessingService;

    private final MinioClient minioClient;

    public KafkaWorker(KafkaTemplate<String, BaseMessage> kafkaTemplate, VideoService videoService, CancelProcessingService cancelProcessingService, MinioClient minioClient)
    {
        this.kafkaTemplate = kafkaTemplate;
        this.videoService = videoService;
        this.cancelProcessingService = cancelProcessingService;
        this.minioClient = minioClient;
    }

    private Optional<String> downloadFile(String bucket, String object)
    {
        String fileName = UUID.randomUUID().toString().replace("-", "");
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
            new File(fileName).delete(); // delete the file if it was created
            return Optional.empty();
        }
    }

    private boolean uploadFile(String bucket, String object, String filePath, Map<String, String> metadata)
    {
        try
        {
            log.info("Uploading object: " + object + ", with metadata: " + metadata);
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .filename(filePath)
                            .userMetadata(metadata)
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

        if(uploadFile(msg.getBucket(), processedObjectName, outputFilePath, Map.of("type", msg.getType().name())) == true)
        {
            response.setProgress(ProcessingProgress.FINISHED);
            response.setObject(processedObjectName);
        }
        if(new File(outputFilePath).delete() == false)
        {
            log.warn("Couldn't delete processed file: " + outputFilePath);
        }
        kafkaTemplate.send(finishedTopicName, response);
        log.info("Worker has sent kafka message with progress: " + response.getProgress().name());
    }

    @KafkaListener(topics="${kafka.topic.canceled}", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void receiveCancelMessages(String processingID)
    {
        this.cancelProcessingService.cancel(processingID);
    }

    @KafkaListener(topics="${kafka.topic.pending}", groupId = "${pending-topic-group-id}")
    public void receivePendingMessage(@Payload(required = false) BaseMessage msg)
    {
        if(msg == null)
        {
            return; // received a tombstone record
        }
        log.info("Worker has received a message with object" + msg.getObject() + " and type: " + msg.getType());
        BaseMessage tempMessage = new BaseMessage(msg);
        tempMessage.setProgress(ProcessingProgress.PROCESSING);
        kafkaTemplate.send(processingTopicName, tempMessage);

        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            Optional<String> outputPath;
            String processedObjectName;

            switch(msg.getType())
            {
                case EXTRACT_AUDIO:
                    processedObjectName = finishedDirectoryPrefix + msg.getFileName() + "/AUDIO.mp4";
                    outputPath = videoService.extractAudio(downloadedFilePath, msg.getProcessingID());
                    break;
                case TRANSCODE:
                    Transcode tempMsg = (Transcode)msg;
                    processedObjectName = finishedDirectoryPrefix + msg.getFileName() + "/"
                            + "TRANSCODED_"
                            + tempMsg.getTargetResolution().getWidth()
                            + "x"
                            + tempMsg.getTargetResolution().getHeight();
                    outputPath = videoService.transcode(downloadedFilePath, tempMsg.getTargetResolution(), msg.getProcessingID());
                    break;
                default:
                    log.warn("Received message with null type: " + msg.getFileName());
                    return;
            }

            handleProcessedFile(msg, processedObjectName, outputPath);
            if(new File(downloadedFilePath).delete() == false)
            {
                log.warn("Couldn't delete downloaded file " + downloadedFilePath);
            }
        });
    }

    /*@KafkaHandler
    public void extractAudio(ExtractAudioMessage msg)
    {
        log.info("Worker has received an extract audio message with object" + msg.getObject() + " and type: " + msg.getType());
        BaseMessage tempMessage = new ExtractAudioMessage(msg);
        tempMessage.setProgress(ProcessingProgress.PROCESSING);
        kafkaTemplate.send(processingTopicName, tempMessage);

        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            String processedObjectName = finishedDirectoryPrefix + msg.getFileName() + "/AUDIO.mp4";
            handleProcessedFile(msg, processedObjectName, videoService.extractAudio(downloadedFilePath));
        });
    }

    @KafkaHandler
    public void transcodeVideo(Transcode msg)
    {
        log.info("Worker has received a transcode video message with file" + msg.getFileName() + " and type: " + msg.getType());
        log.info("Transcode resolution: " + msg.getTargetResolution());
        BaseMessage tempMessage = new ExtractAudioMessage(msg);
        tempMessage.setProgress(ProcessingProgress.PROCESSING);
        kafkaTemplate.send(processingTopicName, tempMessage);

        downloadFile(msg.getBucket(), msg.getObject()).ifPresent(downloadedFilePath ->
        {
            String processedObjectName = finishedDirectoryPrefix + msg.getFileName() + "/"
                    + "TRANSCODED_"
                    + msg.getTargetResolution().getWidth()
                    + "x"
                    + msg.getTargetResolution().getHeight();
            handleProcessedFile(msg, processedObjectName, videoService.transcode(downloadedFilePath, msg.getTargetResolution()));
        });
    }*/
}
