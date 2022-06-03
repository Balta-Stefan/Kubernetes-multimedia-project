package pisio.backend.services;

import java.util.List;

public interface FilesService
{
    List<String> requestPresignUrls(List<String> files, int userID);
    void uploadFinishedNotification(String file, int userID);
}
