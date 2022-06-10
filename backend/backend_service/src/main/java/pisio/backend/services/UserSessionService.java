package pisio.backend.services;

import pisio.backend.models.LoginReply;
import pisio.backend.models.requests.LoginDetails;

public interface UserSessionService
{
    LoginReply login(LoginDetails loginDetails);
}
