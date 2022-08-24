package account.controllers;

import account.entities.EmployeeSalary;
import account.services.EmployeeSalaryService;
import account.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/acct")
public class
AccountController {

    public final EmployeeSalaryService employeeSalaryService;
    private final UserService userService;

    public AccountController(EmployeeSalaryService service, UserService userService){
        this.employeeSalaryService = service;
        this.userService = userService;
    }

    @PutMapping("/payments")
    public ResponseEntity<Object> updatePayment(@Valid @RequestBody EmployeeSalary salary){
        salary.setUserEntity( userService.getByEmailWithExists(salary.getEmployee()) );
        employeeSalaryService.update(salary);
        return new ResponseEntity<>(Map.of(
                "status", "Updated successfully!"
        ), HttpStatus.OK);
    }

    @PostMapping("/payments")
    public ResponseEntity<Object> addPayments(@RequestBody List<@Valid EmployeeSalary> salaries){
        for(EmployeeSalary salary: salaries){
            boolean isExists = employeeSalaryService.isExistsByPeriod(salary.getEmployee(), salary.getPeriod());
            if ( isExists ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error!");
            }
        }
        salaries.forEach(sal -> sal.setUserEntity(userService.getByEmailWithExists(sal.getEmployee()))
        );
        employeeSalaryService.saveAll(salaries);
        return new ResponseEntity<>(Map.of(
                "status", "Added successfully!"
        ), HttpStatus.OK);
    }
}
