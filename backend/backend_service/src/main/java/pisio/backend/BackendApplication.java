package pisio.backend;

import io.minio.MinioClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"pisio.common", "pisio.backend"})
public class BackendApplication
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
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper()
    {
        return new ModelMapper();
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
