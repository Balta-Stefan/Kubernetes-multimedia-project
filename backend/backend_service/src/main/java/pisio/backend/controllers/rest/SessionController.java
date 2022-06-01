package pisio.backend.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pisio.backend.models.requests.LoginDetails;
import pisio.backend.services.UserSessionService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/session")
public class SessionController
{
    private final UserSessionService userSessionService;

    public SessionController(UserSessionService userSessionService)
    {
        this.userSessionService = userSessionService;
    }

    @PostMapping("/login")
    public void login(@RequestBody @Valid LoginDetails loginDetails, Authentication authentication)
    {
        userSessionService.login(loginDetails);
    }

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public void checkStatus(Authentication authentication)
    {

    }
}