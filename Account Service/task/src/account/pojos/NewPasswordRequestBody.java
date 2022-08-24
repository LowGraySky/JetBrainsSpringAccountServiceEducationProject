package account.pojos;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
public class NewPasswordRequestBody {

    @NotEmpty
    @Size(min = 12, message = "Password length must be 12 chars minimum!")
    private String new_password;

}
