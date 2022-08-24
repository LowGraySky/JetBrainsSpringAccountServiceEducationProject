package account.pojos;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ChangeAccessUserRequestBody {

    @NotEmpty
    private String user;

    @Enumerated(EnumType.STRING)
    private ChangeAccessOperation operation;

}
