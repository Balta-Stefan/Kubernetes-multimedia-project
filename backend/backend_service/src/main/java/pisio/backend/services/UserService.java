package pisio.backend.services;

import pisio.backend.models.DTOs.UserDTO;

public interface UserService
{
    UserDTO createUser(UserDTO user);
}
