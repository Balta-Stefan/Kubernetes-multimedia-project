package pisio.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pisio.backend.models.entities.UserEntity;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<UserEntity, Integer>
{
    Optional<UserEntity> findUserEntityByUsernameAndActiveIsTrue(String username);
    Optional<UserEntity> findByEmailAndUsername(String email, String username);
}
