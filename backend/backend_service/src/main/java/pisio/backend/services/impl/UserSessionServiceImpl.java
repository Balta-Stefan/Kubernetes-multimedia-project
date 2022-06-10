package pisio.backend.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.UnauthorizedException;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.models.LoginReply;
import pisio.backend.models.requests.LoginDetails;
import pisio.backend.repositories.UsersRepository;
import pisio.backend.services.UserSessionService;

import java.util.UUID;

@Service
@Slf4j
public class UserSessionServiceImpl implements UserSessionService
{
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsersRepository userRepository;
    private final ModelMapper modelMapper;

    public UserSessionServiceImpl(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, UsersRepository userRepository, ModelMapper modelMapper)
    {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public LoginReply login(LoginDetails loginDetails)
    {
        try
        {
            Authentication authentication = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(loginDetails.getUsername(),
                                    loginDetails.getPassword())
                    );
            AuthenticatedUser userDetails = (AuthenticatedUser)authentication.getPrincipal();
            userDetails.setMessageQueueID(UUID.randomUUID().toString());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return new LoginReply(userDetails.getMessageQueueID());
        }
        catch(BadCredentialsException badCreds)
        {
            log.warn("Someone has tried to log in with bad credentials: ", badCreds);
            throw new UnauthorizedException();
        }
        catch(Exception e)
        {
            log.warn("User session service has thrown an exception: ", e);
            throw new UnauthorizedException();
        }
    }
}
