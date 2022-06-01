package pisio.backend.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.ConflictException;
import pisio.backend.models.DTOs.UserDTO;
import pisio.backend.models.entities.UserEntity;
import pisio.backend.repositories.UsersRepository;
import pisio.backend.services.UserService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class UserServiceImpl implements UserService
{
    private final UsersRepository usersRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public UserServiceImpl(UsersRepository usersRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder)
    {
        this.usersRepository = usersRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO createUser(UserDTO user)
    {
        if(usersRepository.findByEmailAndUsername(user.getEmail(), user.getUsername()).isPresent())
        {
            throw new ConflictException();
        }

        UserEntity userEntity = modelMapper.map(user, UserEntity.class);
        userEntity.setUserID(null);
        userEntity.setActive(true);
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));

        userEntity = usersRepository.saveAndFlush(userEntity);
        entityManager.refresh(userEntity);

        return modelMapper.map(userEntity, UserDTO.class);
    }
}
