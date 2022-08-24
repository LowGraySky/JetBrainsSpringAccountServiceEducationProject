package account.repositories;

import account.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmailIgnoreCase(String userEmail);

    boolean existsByEmailIgnoreCase(String userEmail);
}
