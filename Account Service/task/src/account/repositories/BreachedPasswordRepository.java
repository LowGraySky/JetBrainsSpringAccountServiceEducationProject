package account.repositories;

import account.pojos.BreachedPassword;
import org.springframework.data.repository.CrudRepository;

public interface BreachedPasswordRepository extends CrudRepository<BreachedPassword, Long> {

    boolean existsByPassword(String pass);
}
