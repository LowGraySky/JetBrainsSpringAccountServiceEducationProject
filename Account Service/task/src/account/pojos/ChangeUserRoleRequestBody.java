package account.pojos;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Setter
@Getter
public class ChangeUserRoleRequestBody {

    private String user;

    private String role;

    @Enumerated(value = EnumType.STRING)
    private ChangeRoleOperation operation;
}
