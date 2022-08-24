package account.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Date date;

    @Enumerated(EnumType.STRING)
    private EventType action;

    @NotEmpty
    private String subject;

    @NotEmpty
    private String object;

    @NotEmpty
    private String path;

    public enum EventType {
        CREATE_USER,
        CHANGE_PASSWORD,
        ACCESS_DENIED,
        LOGIN_FAILED,
        GRANT_ROLE,
        REMOVE_ROLE,
        LOCK_USER,
        UNLOCK_USER,
        DELETE_USER,
        BRUTE_FORCE
    }

    public SecurityEvent(Date date, EventType action, String subject, String object, String path){
        this.action = action;
        this.date = date;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
