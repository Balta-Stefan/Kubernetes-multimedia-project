package pisio.backend.controllers.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.models.DTOs.UserDTO;
import pisio.backend.services.FilesService;
import pisio.backend.services.UserService;
import pisio.common.model.DTOs.ProcessingItem;
import pisio.common.model.DTOs.ProcessingRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoRequestController
{
    private final UserService userService;
    private final FilesService filesService;

    public VideoRequestController(UserService userService, FilesService filesService)
    {
        this.userService = userService;
        this.filesService = filesService;
    }

    @PostMapping("/presign-urls")
    public List<String> uploadFiles(@RequestBody List<String> files, @AuthenticationPrincipal AuthenticatedUser user)
    {
        return filesService.requestPresignUrls(files, user);
    }

    @PostMapping("/user")
    public UserDTO register(@RequestBody @Valid UserDTO user)
    {
        return userService.createUser(user);
    }

    @PostMapping("/upload-finished")
    public void notifyUploadFinished(@RequestBody ProcessingRequest request, @AuthenticationPrincipal AuthenticatedUser user)
    {
        filesService.uploadFinishedNotification(request, user);
    }

    @GetMapping("/user/bucket")
    public List<ProcessingItem> listBucket(@AuthenticationPrincipal AuthenticatedUser user)
    {
        return filesService.listBucket(user);
    }

    @DeleteMapping("/file/{fileName}")
    public void deleteObject(@PathVariable String fileName, @AuthenticationPrincipal AuthenticatedUser user)
    {
        filesService.deleteObject(fileName, user);
    }

    @DeleteMapping("/processing/{file}")
    public void stopProcessing(@PathVariable String file, @AuthenticationPrincipal AuthenticatedUser user)
    {
        this.filesService.stopProcessing(file, user);
    }
}
