package account.security;

import account.entities.SecurityEvent;
import account.entities.UserEntity;
import account.services.SecurityEventsService;
import account.services.UserService;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class CustomAuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final int MAX_FAILED_ATTEMPT = 5;
    private final UserService userService;
    private final SecurityEventsService eventsService;
    private final HttpServletRequest request;

    public CustomAuthenticationFailureListener(UserService userService, SecurityEventsService eventsService, HttpServletRequest request){
        this.userService = userService;
        this.eventsService = eventsService;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String requestedUserEmail = ((UsernamePasswordAuthenticationToken) event.getSource()).getPrincipal().toString();

        eventsService.makeEvent(SecurityEvent.EventType.LOGIN_FAILED,
                requestedUserEmail.toLowerCase(),
                request.getRequestURI(),
                request.getRequestURI()
        );

        Optional<UserEntity> user = userService.getByEmail(requestedUserEmail);
        if (user.isPresent()){

            user.get().setFailedAttempt( user.get().getFailedAttempt() + 1 );
            userService.save(user.get());

            if (user.get().getFailedAttempt() >=  MAX_FAILED_ATTEMPT){
                eventsService.makeEvent(SecurityEvent.EventType.BRUTE_FORCE,
                        requestedUserEmail.toLowerCase(),
                        request.getRequestURI(),
                        request.getRequestURI()
                );
                String eventObject = String.format("Lock user %s", requestedUserEmail.toLowerCase());
                userService.lock(user.get(), requestedUserEmail, eventObject, request.getRequestURI());
            }
        }
    }
}
