package account.security;

import account.entities.UserEntity;
import account.services.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class CustomAuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    public final UserService userService;
    private final HttpServletRequest request;

    public CustomAuthenticationSuccessListener(UserService userService, HttpServletRequest request){
        this.userService = userService;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String userEmail = event.getAuthentication().getName();
        UserEntity user = userService.getByEmailWithExists(userEmail);

        if (user.getFailedAttempt() > 0){
            user.setFailedAttempt(0);
            userService.save(user);
            if (user.isBlocked()){
                String eventObject = String.format("Unlock user %s", userEmail.toLowerCase());
                userService.unlock(user, userEmail, eventObject, request.getRequestURI());
            }
        }

    }
}
