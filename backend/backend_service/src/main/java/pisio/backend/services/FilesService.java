package pisio.backend.services;

import pisio.backend.models.AuthenticatedUser;
import pisio.common.model.DTOs.ProcessingItem;

import java.util.List;

public interface FilesService
{
    List<String> requestPresignUrls(List<String> files, AuthenticatedUser user);
    ProcessingItem uploadFinishedNotification(String file, AuthenticatedUser user);
    List<String> listBucket(AuthenticatedUser user);
}
