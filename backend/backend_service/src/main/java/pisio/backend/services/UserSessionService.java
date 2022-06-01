package pisio.backend.services;

import pisio.backend.models.requests.LoginDetails;

public interface UserSessionService
{
    void login(LoginDetails loginDetails);
}
