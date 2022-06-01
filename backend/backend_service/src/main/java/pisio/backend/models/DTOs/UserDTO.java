package pisio.backend.models.DTOs;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserDTO
{
    private final int userID;

    @NotBlank
    private final String username;

    @NotBlank
    private final String password;

    @NotBlank
    private final String email;

    private final boolean active;
}
