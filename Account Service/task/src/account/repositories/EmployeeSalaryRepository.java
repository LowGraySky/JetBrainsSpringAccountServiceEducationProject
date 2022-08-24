package account.repositories;

import account.entities.EmployeeSalary;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeSalaryRepository extends CrudRepository<EmployeeSalary, Long> {

    List<EmployeeSalary> findByEmployeeIgnoreCaseOrderByPeriodDesc(String userEmail);

    Optional<EmployeeSalary> findByEmployeeIgnoreCaseAndPeriod(String userEmail, String period);

    boolean existsByEmployeeIgnoreCaseAndPeriod(String userEmail, String period);


}

