package pisio.backend.models.requests;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDetails
{
    @NotBlank
    private final String username;

    @NotBlank
    private final String password;
}