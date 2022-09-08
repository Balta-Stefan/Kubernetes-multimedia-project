package pisio.backend.services;

import io.minio.http.Method;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.models.DTOs.PresignedUploadLink;
import pisio.common.model.DTOs.ProcessingItem;
import pisio.common.model.DTOs.ProcessingRequest;
import pisio.common.model.DTOs.ProcessingRequestReply;

import java.util.List;

public interface FilesService
{
    List<PresignedUploadLink> requestPresignUrls(List<String> files, AuthenticatedUser user);
    String createPresignURL(String bucket, String object, int expiryHours, Method method);
    List<ProcessingRequestReply> uploadFinishedNotification(ProcessingRequest request, AuthenticatedUser user);
    List<ProcessingItem> listBucket(AuthenticatedUser user);
    boolean deleteObject(String bucket, String object, boolean recursive);
    void deletePendingObject(String file, AuthenticatedUser user);
    void stopProcessing(String file, String processingID, AuthenticatedUser user);
}
