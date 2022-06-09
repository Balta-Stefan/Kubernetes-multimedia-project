package pisio.backend.services.impl;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pisio.backend.exceptions.ConflictException;
import pisio.backend.models.DTOs.UserDTO;
import pisio.backend.models.entities.UserEntity;
import pisio.backend.repositories.UsersRepository;
import pisio.backend.services.UserService;
import pisio.common.utils.BucketNameCreator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService
{
    private final UsersRepository usersRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final MinioClient minioClient;

    @PersistenceContext
    private EntityManager entityManager;

    public UserServiceImpl(UsersRepository usersRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, MinioClient minioClient)
    {
        this.usersRepository = usersRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.minioClient = minioClient;
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

        try
        {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(BucketNameCreator.createBucket(userEntity.getUserID()))
                            .build());
        }
        catch(Exception e)
        {
            log.warn("Couldn't create user bucket: " + e.getMessage());
            e.printStackTrace();
        }

        return modelMapper.map(userEntity, UserDTO.class);
    }
}
