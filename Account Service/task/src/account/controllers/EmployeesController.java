package account.controllers;


import account.entities.EmployeeSalary;
import account.entities.UserEntity;
import account.services.EmployeeSalaryService;
import account.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/empl")
@Validated
public class EmployeesController {

    private final UserService userService;
    private final EmployeeSalaryService employeeSalaryService;

    public EmployeesController(UserService service, EmployeeSalaryService employeeSalaryService){
        this.employeeSalaryService = employeeSalaryService;
        this.userService = service;
    }

    @GetMapping("/payment")
    public ResponseEntity<Object> getEmployeePayment(Authentication authentication,
                                                     @RequestParam(name = "period", required = false)
                                                     @Pattern(regexp = "^(0[1-9]|1[0-2])-[0-9]{4}")
                                                     String period) {
        List<EmployeeSalary> employeeSalaries = new ArrayList<>();
        UserDetails details = (UserDetails) authentication.getPrincipal();
        UserEntity userEntity = userService.getByEmailWithExists(details.getUsername());
        if (period == null){
            employeeSalaries = employeeSalaryService.getAllOrderByDate(userEntity.getEmail());
        }else{
            employeeSalaries.add(employeeSalaryService.getByEmployeeAndPeriod(userEntity.getEmail(), period));
        }
        List<Object> result = employeeSalaries.stream().map(salary -> Map.of(
                "name", userEntity.getName(),
                "lastname", userEntity.getLastname(),
                "period", Month.of(
                            Integer.parseInt(salary.getPeriod().split("-")[0])
                            ).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "-" +
                            salary.getPeriod().split("-")[1],
                "salary", salary.getSalary()/100 + " dollar(s) " + salary.getSalary()%100 + " cent(s)"
        )).collect(Collectors.toList());

        return new ResponseEntity<>(result.isEmpty() ? List.of() : (result.size() == 1 ? result.get(0) : result), HttpStatus.OK);
    }
}
