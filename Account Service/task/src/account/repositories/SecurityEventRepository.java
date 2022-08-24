package account.repositories;

import account.entities.SecurityEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SecurityEventRepository extends CrudRepository<SecurityEvent, Long> {

}
