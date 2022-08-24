package account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Entity
@Data
public class EmployeeSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "userentity_id")
    private UserEntity userEntity;

    @NotEmpty
    private String employee;

    @Pattern(regexp = "^(0[1-9]|1[0-2])-[0-9]{4}")
    private String period;

    @Min(0)
    private long salary;
}
