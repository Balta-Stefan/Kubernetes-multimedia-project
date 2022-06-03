package pisio.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pisio.common", "pisio.worker"})
@EnableScheduling
public class WorkerApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
