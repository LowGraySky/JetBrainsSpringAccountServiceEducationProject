package account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Getter
@Setter
public class UserEntity {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String lastname;

    @NotEmpty
    @Pattern(regexp = ".+@acme\\.com")
    @Column(unique = true)
    private String email;

    @NotEmpty
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    private String password;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "users", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role", referencedColumnName = "id"))
    private Set<Group> groups;

    @OneToMany(mappedBy = "userEntity")
    private List<EmployeeSalary> salaries;

    @Column(name = "failed_attempt")
    private int failedAttempt = 0;

    @Column(name = "is_blocked")
    private boolean isBlocked;

}
