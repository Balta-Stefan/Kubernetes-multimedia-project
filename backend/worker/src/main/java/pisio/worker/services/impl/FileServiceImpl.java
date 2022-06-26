package pisio.worker.services.impl;

import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.springframework.stereotype.Service;
import pisio.worker.services.FileService;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService
{
    private final MinioClient minioClient;

    public FileServiceImpl(MinioClient minioClient)
    {
        this.minioClient = minioClient;
    }

    @Override
    public Optional<File> downloadFile(String bucket, String object)
    {
        String fileName = UUID.randomUUID().toString().replace("-", "");
        return downloadFile(bucket, object, fileName);
    }

    @Override
    public Optional<File> downloadFile(String bucket, String object, String outputFileName)
    {
        File file = new File(outputFileName);

        try
        {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .filename(outputFileName)
                            .build());

            return Optional.of(file);
        }
        catch(Exception e)
        {
            file.delete(); // delete the file if it was created
            return Optional.empty();
        }
    }

    @Override
    public boolean uploadFile(String bucket, String object, File file, Map<String, String> metadata)
    {
        try
        {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .filename(file.getName())
                            .userMetadata(metadata)
                            .build());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
