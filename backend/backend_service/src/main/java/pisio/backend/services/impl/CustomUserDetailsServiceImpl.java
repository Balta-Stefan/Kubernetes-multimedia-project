package pisio.backend.services.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pisio.backend.models.AuthenticatedUser;
import pisio.backend.models.entities.UserEntity;
import pisio.backend.repositories.UsersRepository;
import pisio.backend.services.CustomUserDetailsService;
import org.modelmapper.ModelMapper;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService
{
    private final UsersRepository usersRepository;
    private final ModelMapper modelMapper;

    public CustomUserDetailsServiceImpl(UsersRepository usersRepository, ModelMapper modelMapper)
    {
        this.usersRepository = usersRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserEntity user = usersRepository.findUserEntityByUsernameAndActiveIsTrue(username).orElseThrow(() ->
        {
            throw new UsernameNotFoundException("Username not found");
        });


        return modelMapper.map(user, AuthenticatedUser.class);
    }
}
