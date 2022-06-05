package pisio.worker;

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
import pisio.worker.services.AudioService;
import pisio.worker.services.VideoService;

@KafkaListener(id = "${pending-topic-group-id}", topics = "${pending.topic-name}")
@Slf4j
@Service
public class KafkaWorker
{
    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;
    @Value("${finished.topic-name}")
    private String finishedTopicName;
    private final AudioService audioService;
    private final VideoService videoService;

    public KafkaWorker(KafkaTemplate<String, BaseMessage> kafkaTemplate, AudioService audioService, VideoService videoService)
    {
        this.kafkaTemplate = kafkaTemplate;
        this.audioService = audioService;
        this.videoService = videoService;
    }

    @KafkaHandler
    public void extractAudio(ExtractAudioMessage msg)
    {
        log.warn("Worker has received an extract audio message: " + msg.toString());
        boolean status = audioService.extractAudio(msg);

        BaseMessage response = new BaseMessage(msg);

        if(status == true)
        {
            response.setProgress(ProcessingProgress.FINISHED);
        }
        else
        {
            response.setProgress(ProcessingProgress.FAILED);
        }
        kafkaTemplate.send(finishedTopicName, response);
    }

    @KafkaHandler
    public void transcodeVideo(Transcode msg)
    {
        log.info("Worker has received a transcode message: " + msg.toString());
        boolean status = videoService.transcode(msg);

        BaseMessage response = new BaseMessage(msg);

        if(status == true)
        {
            response.setProgress(ProcessingProgress.FINISHED);
        }
        else
        {
            response.setProgress(ProcessingProgress.FAILED);
        }

        kafkaTemplate.send(finishedTopicName, response);
    }
}
