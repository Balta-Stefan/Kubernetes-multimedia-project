package pisio.backend.services;

import io.minio.http.Method;
import pisio.backend.models.AuthenticatedUser;
import pisio.common.model.DTOs.ProcessingItem;

import java.util.List;

public interface FilesService
{
    List<String> requestPresignUrls(List<String> files, AuthenticatedUser user);
    String createPresignURL(String bucket, String object, int expiryHours, Method method);
    ProcessingItem uploadFinishedNotification(String file, AuthenticatedUser user);
    List<ProcessingItem> listBucket(AuthenticatedUser user);
    void deleteObject(String object, AuthenticatedUser user);
}
