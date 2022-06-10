package pisio.backend.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import pisio.backend.models.requests.LoginDetails;
import pisio.backend.services.UserSessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public void login(@RequestBody @Valid LoginDetails loginDetails)
    {
        userSessionService.login(loginDetails);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setClearAuthentication(true);
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.logout(request, response, authentication);
    }

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public void checkStatus(Authentication authentication)
    {

    }
}