package pisio.backend.controllers.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pisio.backend.models.DTOs.UserDTO;
import pisio.backend.services.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class VideoRequestController
{
    private final UserService userService;

    public VideoRequestController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/submit")
    public void uploadFiles(MultipartFile[] files)
    {

    }

    @PostMapping("/user")
    public UserDTO register(@RequestBody @Valid UserDTO user)
    {
        return userService.createUser(user);
    }
}
