package account.controllers;

import account.entities.Group;
import account.entities.UserEntity;
import account.exceptions.AdministratorDeleteException;
import account.pojos.ChangeAccessUserRequestBody;
import account.pojos.ChangeUserRoleRequestBody;
import account.services.UserService;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService){
        this.userService = userService;
    }

    @PutMapping("/user/role")
    public ResponseEntity<Object> changeUserRole(HttpServletRequest request, Authentication authentication, @RequestBody ChangeUserRoleRequestBody body) throws NotFoundException {
        UserEntity user = userService.getByEmailWithExists(body.getUser());
        Group requestedGroup = userService.findGroupByName("ROLE_" + body.getRole());

        String eventObject = "Grant role " + body.getRole() + " to " + user.getEmail();
        switch (body.getOperation()) {
            case GRANT:
                if ((userService.isAdmin(user) && userService.getBusinessGroups().contains(requestedGroup))
                        || (!userService.isAdmin(user) && requestedGroup.equals(userService.getAdminGroup()))
                )
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
                userService.appendToGroup(user, requestedGroup, authentication.getName(), eventObject, request.getRequestURI());
                break;
            case REMOVE:
                if (userService.isAdmin(user) && requestedGroup.equals(userService.getAdminGroup()))
                    throw new AdministratorDeleteException();
                if (!user.getGroups().contains(requestedGroup))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                if (user.getGroups().size() == 1)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");

                eventObject = "Remove role " + body.getRole() + " from " + user.getEmail();
                userService.excludeFromGroup(user, requestedGroup, authentication.getName(), eventObject, request.getRequestURI());
                break;
        }
        return new ResponseEntity<>(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "lastname", user.getLastname(),
                "email", user.getEmail().toLowerCase(),
                "roles", userService.loadUserByUsername(user.getEmail()).getAuthorities().stream().map(Object::toString).collect(Collectors.toList())
        ), HttpStatus.OK);
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<Object> deleteUser(HttpServletRequest request, Authentication authentication, @PathVariable(name = "email") String userEmail){
        UserEntity user = userService.getByEmailWithExists(userEmail.isEmpty() ? userEmail : userEmail.strip());
        Group adminGroup = userService.findGroupByName("ROLE_ADMINISTRATOR");
        if (user.getGroups().contains(adminGroup) ) {
            throw new AdministratorDeleteException();
        }
        String eventSubject = ((UserDetails) authentication.getPrincipal()).getUsername();
        userService.delete(user, eventSubject, user.getEmail(), request.getRequestURI());
        return new ResponseEntity<>(Map.of(
                "user", user.getEmail().toLowerCase(),
                "status", "Deleted successfully!"
        ), HttpStatus.OK);
    }

    @RequestMapping(value = "/user",
            produces = "application/json",
            method = {RequestMethod.GET, RequestMethod.DELETE}
    )
    public ResponseEntity<Object> getAllUsers(){
        List<Object> result = userService.getAll().stream()
                .sorted(Comparator.comparing(UserEntity::getId))
                .map(user -> Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "lastname", user.getLastname(),
                        "email", user.getEmail().toLowerCase(),
                        "roles", userService.loadUserByUsername(user.getEmail()).getAuthorities().stream().map(Object::toString).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("user/access")
    public ResponseEntity<Object> changeUserAccess(HttpServletRequest request , Authentication authentication, @Valid @RequestBody ChangeAccessUserRequestBody body){
        UserEntity user = userService.getByEmailWithExists(body.getUser());
        Group adminGroup = userService.findGroupByName("ROLE_ADMINISTRATOR");

        String eventSubject = ((UserDetails) authentication.getPrincipal()).getUsername();
        String eventObject = String.format("Lock user %s", user.getEmail().toLowerCase());

        switch (body.getOperation()){
            case LOCK:
                if (user.getGroups().contains(adminGroup))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
                userService.lock(user, eventSubject, eventObject, request.getRequestURI());
                break;
            case UNLOCK:
                eventObject = String.format("Unlock user %s", user.getEmail().toLowerCase());
                userService.unlock(user, eventSubject, eventObject, request.getRequestURI());
                break;
        }
        return new ResponseEntity<>(Map.of(
                "status", String.format("User %s %sed!",user.getEmail().toLowerCase(), body.getOperation().toString().toLowerCase())),
                HttpStatus.OK);
    }
}
