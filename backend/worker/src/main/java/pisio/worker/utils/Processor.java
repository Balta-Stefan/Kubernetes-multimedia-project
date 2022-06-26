package pisio.worker.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class Processor implements Callable<Boolean>
{
    private final List<String> command;
    @Getter
    private final String processingID;
    private Process process;
    private boolean canceled = false;

    public Processor(List<String> command, String processingID)
    {
        this.command = command;
        this.processingID = processingID;
    }

    @Override
    public Boolean call()
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.inheritIO();

        log.info("Video processor has received command: " + command);

        try
        {
            process = pb.start();
            process = process.onExit().get();

            if(canceled == true)
                return false;

            if(process.exitValue() != 0)
            {
                log.warn("Video processor exit code is not 0");
                return false;
            }
        }
        catch (Exception e)
        {
            log.warn("An exception has occured while trying to process a video:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void cancelProcessing()
    {
        canceled = true;
        process.destroyForcibly();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Processor)) return false;
        Processor that = (Processor) o;
        return processingID.equals(that.processingID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(processingID);
    }
}
