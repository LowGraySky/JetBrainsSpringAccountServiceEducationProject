package account;

import account.entities.Group;
import account.pojos.BreachedPassword;
import account.repositories.BreachedPasswordRepository;
import account.repositories.GroupRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader {

    private final GroupRepository groupRepository;
    private final BreachedPasswordRepository breachedPasswordRepository;

    public DataLoader(GroupRepository groupRepository, BreachedPasswordRepository breachedPasswordRepository){
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.groupRepository = groupRepository;
        createBreachedPasswords();
        createRoles();
    }

    private void createRoles(){
        String[] groups = new String[]{"ROLE_ADMINISTRATOR", "ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_AUDITOR"};
        if ( ((List<Group>) groupRepository.findAll()).isEmpty() )
            Arrays.stream(groups).forEach(group -> groupRepository.save(new Group(group)));
    }

    private void createBreachedPasswords(){
        String[] months = new String[]{"PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
                "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"
        };
        if ( ((List<BreachedPassword>) breachedPasswordRepository.findAll()).isEmpty())
            Arrays.stream(months).forEach(pass -> breachedPasswordRepository.save(new BreachedPassword(pass)));
    }
}
