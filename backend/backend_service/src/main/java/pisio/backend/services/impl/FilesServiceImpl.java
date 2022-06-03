package pisio.backend.services.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.InternalServerError;
import pisio.backend.services.FilesService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FilesServiceImpl implements FilesService
{
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access_key}")
    private String accessKey;

    @Value("${minio.secret_access_key}")
    private String secret;

    @Value("${pending.topic}")
    private String pendingTopic;

    @Value("${finished.topic}")
    private String finishedTopic;

    private MinioClient minioClient;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public FilesServiceImpl(KafkaTemplate<String, String> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostConstruct
    void test()
    {
        minioClient = MinioClient.builder()
                .endpoint("localhost", 80, false)
                .credentials(accessKey, secret)
                .build();
    }

    @Override
    public List<String> requestPresignUrls(List<String> files, int userID)
    {
        // Get presigned URL string to upload 'my-objectname' in 'my-bucketname'
        // with an expiration of 1 day.

        try
        {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-" + userID).build()) == false)
            {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket("user-" + userID).build());
            }
        }
        catch(Exception e)
        {
            log.warn("Couldn't create a bucket: " + e.getMessage());
            e.printStackTrace();
            throw new InternalServerError();
        }

        try
        {
            List<String> presignedURLs = new ArrayList<>();
            for(String f : files)
            {
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket("user-" + userID)
                                .object("pending/" + f)
                                .expiry(1, TimeUnit.HOURS)
                                .build());
                presignedURLs.add(url);
            }

            return presignedURLs;
        }
        catch(Exception e)
        {
            log.warn("Presigning a bucket failed: " + e.getMessage());
            e.printStackTrace();
            throw new InternalServerError();
        }

    }

    @Override
    public void uploadFinishedNotification(String file, int userID)
    {
        kafkaTemplate.send(pendingTopic, file);
    }
}
