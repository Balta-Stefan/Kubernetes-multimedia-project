package pisio.worker;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"pisio.common", "pisio.worker"})
@EnableKafka
public class WorkerApplication
{
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.port}")
    private int minioEndpointPort;

    @Value("${minio.access_key}")
    private String accessKey;

    @Value("${minio.secret_access_key}")
    private String secret;

    public static void main(String[] args)
    {
        SpringApplication.run(WorkerApplication.class, args);
    }

    @Bean
    public MinioClient minioClient()
    {
        return MinioClient.builder()
                .endpoint(minioEndpoint, minioEndpointPort, false)
                .credentials(accessKey, secret)
                .build();
    }
}
