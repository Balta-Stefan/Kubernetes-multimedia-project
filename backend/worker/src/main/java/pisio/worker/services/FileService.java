package pisio.worker.services;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface FileService
{
    Optional<File> downloadFile(String bucket, String object);
    Optional<File> downloadFile(String bucket, String object, String outputFileName);
    boolean uploadFile(String bucket, String object, File file, Map<String, String> metadata);
}
