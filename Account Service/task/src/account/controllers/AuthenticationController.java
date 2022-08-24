package account.controllers;

import account.entities.UserEntity;
import account.exceptions.UserExistsException;
import account.pojos.NewPasswordRequestBody;
import account.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

import java.util.stream.Collectors;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> singUp(HttpServletRequest request, Authentication authentication, @Valid @RequestBody UserEntity userEntity){
        if(userService.getByEmail(userEntity.getEmail()).isPresent() ){
            throw new UserExistsException();
        }
        String eventSubject = authentication  == null ? "Anonymous":  ((UserDetails) authentication.getPrincipal()).getUsername();
        userService.register(userEntity, eventSubject, userEntity.getEmail(), request.getRequestURI());
        UserEntity newUser = userService.getByEmail(userEntity.getEmail()).get();
        return new ResponseEntity<>(Map.of(
                "id", newUser.getId(),
                "name", newUser.getName(),
                "lastname", newUser.getLastname(),
                "email", newUser.getEmail(),
                "roles", userService.loadUserByUsername(
                        newUser.getEmail()).getAuthorities()
                        .stream()
                        .map(GrantedAuthority::toString)
                        .collect(Collectors.toList()
                )
        ), HttpStatus.OK);
    }

    @PostMapping("/changepass")
    public ResponseEntity<Object> changePassword(HttpServletRequest request, Authentication authentication, @Valid @RequestBody NewPasswordRequestBody body){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity userEntity = userService.getByEmailWithExists(userDetails.getUsername());
        String eventSubject = ((UserDetails) authentication.getPrincipal()).getUsername();
        userService.changePassword(userEntity, body.getNew_password(), eventSubject, userEntity.getEmail(), request.getRequestURI());
        return new ResponseEntity<>(Map.of(
                "email", userEntity.getEmail().toLowerCase(),
                "status", "The password has been updated successfully"
        ), HttpStatus.OK);
    }
}
