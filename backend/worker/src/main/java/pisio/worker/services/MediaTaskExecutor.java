package pisio.worker.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pisio.common.model.enums.ProcessingProgress;
import pisio.common.model.messages.BaseMessage;
import pisio.common.model.messages.ExtractAudioMessage;
import pisio.common.model.messages.Transcode;
import pisio.worker.utils.Processor;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class MediaTaskExecutor
{
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class JobWrapper
    {
        @EqualsAndHashCode.Exclude
        public final Processor processor;
        public final BaseMessage metadata;
        public final File inputFile;
        public final File outputFile;
    }

    private final NotificationSender notificationSender;
    private final FileService fileService;
    private final Queue<JobWrapper> pendingTasks = new LinkedList<>();
    private final Map<String, JobWrapper> runningJobs = new HashMap<>();
    private final AtomicInteger inputFileNameIdGenerator = new AtomicInteger(0);

    private AtomicBoolean isDownloading = new AtomicBoolean(false); // only one download is allowed

    public MediaTaskExecutor(NotificationSender notificationSender, FileService fileService)
    {
        this.notificationSender = notificationSender;
        this.fileService = fileService;
    }

    public boolean checkCanProcessNewMessage()
    {
        return (isDownloading.get() == false) && hasResourcesForJob();
    }

    public void stopProcess(String processingID)
    {
        synchronized (pendingTasks)
        {
            pendingTasks
                    .stream()
                    .filter(j -> j.metadata.getProcessingID().equals(processingID))
                    .findFirst()
                    .ifPresent(pendingTasks::remove);
        }

        JobWrapper runningJob;
        synchronized(runningJobs)
        {
            runningJob = runningJobs.remove(processingID);
        }
        if (runningJob != null)
        {
            runningJob.processor.cancelProcessing();
            BaseMessage jobMetadata = runningJob.metadata;
            jobMetadata.setProgress(ProcessingProgress.FAILED);
            notificationSender.sendNotification(jobMetadata);

            runningJob.inputFile.delete();
            runningJob.outputFile.delete();
        }
    }

    private void taskFinishedCallback(JobWrapper job, Boolean success)
    {
        JobWrapper finishedJob;
        synchronized (runningJobs)
        {
            finishedJob = runningJobs.remove(job.metadata.getProcessingID());
        }

        if(finishedJob != null)
        {
            BaseMessage msg = finishedJob.metadata;
            ProcessingProgress status = (success == true) ? ProcessingProgress.FINISHED : ProcessingProgress.FAILED;
            msg.setProgress(status);

            if(success == true)
            {
                if(finishedJob.outputFile.exists())
                {
                    fileService.uploadFile(msg.getBucket(), msg.getProcessedObjectName(), finishedJob.outputFile, Collections.emptyMap());
                }
                else
                {
                    log.warn("A job has been successfully completed but no output file has been found.");
                    msg.setProgress(ProcessingProgress.FAILED);
                }
            }

            notificationSender.sendNotification(msg);

            finishedJob.inputFile.delete();
            finishedJob.outputFile.delete();
        }
        else
        {
            log.warn("TaskExecutor couldn't find finished job in the runningJobs HashMap");
        }


        // schedule new task
        JobWrapper newJob;
        synchronized (pendingTasks)
        {
            newJob = pendingTasks.poll();
        }

        if(newJob != null)
        {
            scheduleIfPossible(newJob);
        }
    }

    private boolean hasResourcesForJob()
    {
        synchronized (runningJobs)
        {
            return runningJobs.size() == 0; // can process only one job.CPU and memory usage should be examined here instead of using a fixed limit
        }
    }

    private void scheduleIfPossible(JobWrapper job)
    {
        if (hasResourcesForJob())
        {
            synchronized (runningJobs)
            {
                runningJobs.put(job.metadata.getProcessingID(), job);
                CompletableFuture.supplyAsync(job.processor::call)
                        .thenAccept(status -> taskFinishedCallback(job, status));
            }
        }
        else
        {
            synchronized (pendingTasks)
            {
                pendingTasks.add(job);
            }
        }
    }

    private void schedule(List<String> command, BaseMessage msg, String inputFileName)
    {
        Processor processor = new Processor(command, msg.getProcessingID());

        CompletableFuture.supplyAsync(() ->
        {
            isDownloading.set(true);

            msg.setProgress(ProcessingProgress.PROCESSING);
            notificationSender.sendNotification(msg);

            Optional<File> fileOpt = fileService.downloadFile(msg.getBucket(), msg.getObject(), inputFileName);
            isDownloading.set(false);
            return fileOpt;
        }).thenAccept((Optional<File> fileOpt) ->
        {
            if(fileOpt.isEmpty())
            {
                msg.setProgress(ProcessingProgress.FAILED);
                notificationSender.sendNotification(msg);
                return;
            }

            File inputFile = fileOpt.get();
            File outputFile = new File(inputFile.getParent(), "OUT_" + inputFile.getName());

            JobWrapper job = new JobWrapper(processor, msg, inputFile, outputFile);
            scheduleIfPossible(job);
        });
    }

    public void schedule(Transcode msg)
    {
        String inputFileName = inputFileNameIdGenerator.getAndIncrement() + ".mp4";

        List<String> command = List.of(
                "ffmpeg",
                "-i",
                inputFileName,
                "-vf",
                "scale=" + msg.getTargetResolution().getWidth() + ":" + msg.getTargetResolution().getHeight(),
                "-c:a",
                "copy",
                "OUT_" + inputFileName);
        schedule(command, msg, inputFileName);
    }

    public void schedule(ExtractAudioMessage msg)
    {
        String inputFileName = inputFileNameIdGenerator.getAndIncrement() + ".mp4";

        List<String> command = List.of(
                "ffmpeg",
                "-i",
                inputFileName,
                "-map",
                "0:a",
                "-c",
                "copy",
                "OUT_" + inputFileName);
        schedule(command, msg, inputFileName);
    }
}
