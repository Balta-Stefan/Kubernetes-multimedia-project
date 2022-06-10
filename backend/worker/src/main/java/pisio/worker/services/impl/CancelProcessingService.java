package pisio.worker.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CancelProcessingService
{
    private final Map<String, Process> processes = new HashMap<>();

    public void deregisterProcess(String processingID)
    {
        synchronized (processes)
        {
            processes.remove(processingID);
        }
    }

    public void registerProcess(String processingID, Process process)
    {
        synchronized (processes)
        {
            processes.put(processingID, process);
        }
    }
    public void cancel(String processingID)
    {
        Process process;
        synchronized (processes)
        {
            process = processes.remove(processingID);
        }
        if(process != null)
        {
            process.destroyForcibly();
        }
    }
}
