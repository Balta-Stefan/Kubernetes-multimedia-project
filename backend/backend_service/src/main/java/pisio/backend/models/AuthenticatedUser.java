package pisio.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class AuthenticatedUser implements UserDetails
{
    private final int userID;
    private final String username;
    private final boolean active;
    private final String password;
    private final List<GrantedAuthority> authorities = Collections.emptyList();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return active;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return active;
    }

    @Override
    public boolean isEnabled()
    {
        return active;
    }
}
