package pisio.backend.services;

import io.minio.http.Method;
import pisio.backend.models.AuthenticatedUser;
import pisio.common.model.DTOs.ProcessingRequest;
import pisio.common.model.DTOs.UserNotification;

import java.util.List;

public interface FilesService
{
    List<String> requestPresignUrls(List<String> files, AuthenticatedUser user);
    String createPresignURL(String bucket, String object, int expiryHours, Method method);
    void uploadFinishedNotification(ProcessingRequest request, AuthenticatedUser user);
    List<UserNotification> listBucket(AuthenticatedUser user);
    boolean deleteObject(String object, AuthenticatedUser user);
    void stopProcessing(String file, AuthenticatedUser user);
}