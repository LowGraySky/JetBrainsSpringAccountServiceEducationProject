package account.exceptions;

import account.entities.SecurityEvent;
import account.entities.UserEntity;
import account.services.SecurityEventsService;
import account.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccessDeniedHandlerExceptionHandler implements AccessDeniedHandler {

    private final UserService userService;
    private final SecurityEventsService eventService;

    public AccessDeniedHandlerExceptionHandler(UserService userService, SecurityEventsService eventsService){
        this.userService = userService;
        this.eventService = eventsService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        UserEntity user = userService.getByEmailWithExists(request.getUserPrincipal().getName());
        eventService.makeEvent(
                SecurityEvent.EventType.ACCESS_DENIED,
                user.getEmail().toLowerCase(),
                request.getRequestURI(),
                request.getRequestURI()
        );
        response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied!");
    }
}
