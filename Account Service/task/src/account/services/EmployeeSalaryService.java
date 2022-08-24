package account.services;

import account.entities.EmployeeSalary;
import account.repositories.EmployeeSalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(rollbackFor = { SQLException.class })
public class EmployeeSalaryService {

    public final EmployeeSalaryRepository repository;

    public EmployeeSalaryService(EmployeeSalaryRepository repository){
        this.repository = repository;
    }

    public Iterable<EmployeeSalary> saveAll(List<EmployeeSalary> salaries){
        return repository.saveAll(salaries);
    }

    public EmployeeSalary update(EmployeeSalary salary){
        EmployeeSalary newSalary = getByEmployeeAndPeriod(salary.getEmployee(), salary.getPeriod());
        newSalary.setEmployee(salary.getEmployee());
        newSalary.setSalary(salary.getSalary());
        newSalary.setPeriod(salary.getPeriod());
        return repository.save(newSalary);
    }

    public List<EmployeeSalary> getAllOrderByDate(String userEmail){
        return repository.findByEmployeeIgnoreCaseOrderByPeriodDesc(userEmail);
    }

    public EmployeeSalary getByEmployeeAndPeriod(String userEmail, String period){
        Optional<EmployeeSalary> result = repository.findByEmployeeIgnoreCaseAndPeriod(userEmail, period);
        if(result.isEmpty()){
            throw new NoSuchElementException("email: " + userEmail + ", period: " + period);
        }
        return result.get();
    }

    public boolean isExistsByPeriod(String userEmail, String period){
        return repository.existsByEmployeeIgnoreCaseAndPeriod(userEmail, period);
    }
}
