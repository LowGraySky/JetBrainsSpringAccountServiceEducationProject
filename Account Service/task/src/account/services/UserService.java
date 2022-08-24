package account.services;

import account.entities.Group;
import account.entities.SecurityEvent;
import account.entities.UserEntity;
import account.exceptions.*;

import account.repositories.BreachedPasswordRepository;
import account.repositories.GroupRepository;
import account.repositories.UserRepository;
import javassist.NotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder encoder;
    private final BreachedPasswordRepository breachedPasswordRepository;
    private final SecurityEventsService eventsService;

    public UserService(UserRepository userRepository, GroupRepository groupRepository, PasswordEncoder encoder, BreachedPasswordRepository breachedPasswordRepository, SecurityEventsService eventsService){
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.encoder = encoder;
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.eventsService = eventsService;
    }

    public List<UserEntity> getAll(){
        return (List<UserEntity>) userRepository.findAll();
    }

    public void delete(UserEntity user, String subject, String object, String path){
        userRepository.delete(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.DELETE_USER,
                subject.toLowerCase(),
                object.toLowerCase(),
                path
        );
    }

    public Group findGroupByName(String groupName){
        Optional<Group> group = groupRepository.findByNameIgnoreCase(groupName);
        if (group.isEmpty())
            throw new GroupNotFoundException();
        return group.get();
    }

    public void register(UserEntity userEntity, String subject, String object, String path){
        if(getByEmail(userEntity.getEmail()).isPresent())
            throw new UserExistsException();
        if(isPasswordBreached(userEntity.getPassword()))
            throw new PasswordBreachedException();
        userEntity.setPassword(encoder.encode(userEntity.getPassword()));

        Group group = findGroupByName("ROLE_USER");
        if (((List<UserEntity>) userRepository.findAll() ).isEmpty()) {
            group = findGroupByName("ROLE_ADMINISTRATOR");
        }
        if (userEntity.getGroups() == null){
            userEntity.setGroups(new HashSet<>());
        }
        userEntity.getGroups().add(group);
        save(userEntity);
        eventsService.makeEvent(
                SecurityEvent.EventType.CREATE_USER,
                subject,
                object.toLowerCase(),
                path
        );
    }

    public UserEntity save(UserEntity entity){
        entity.setEmail(entity.getEmail().toLowerCase());
        return userRepository.save(entity);
    }

    public UserEntity getByEmailWithExists(String email) {
        Optional<UserEntity> user =  userRepository.findByEmailIgnoreCase(email);
        if (user.isEmpty())
            throw new UserNotExistsException();
        return user.get();
    }

    public Optional<UserEntity> getByEmail(String email){
        return userRepository.findByEmailIgnoreCase(email);
    }

    public void appendToGroup(UserEntity user, Group group, String subject, String object, String path){
        if (user.getGroups() == null){
            user.setGroups(new HashSet<>());
        }
        user.getGroups().add(group);
        save(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.GRANT_ROLE,
                subject.toLowerCase(),
                object,
                path
        );
    }

    public void excludeFromGroup(UserEntity user, Group group, String subject, String object, String path) throws NotFoundException {
        if (user.getGroups() == null)
            user.setGroups(new HashSet<>());
        if (!user.getGroups().contains(group))
            throw new NotFoundException("The user does not have a role!");
        user.getGroups().remove(group);
        save(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.REMOVE_ROLE,
                subject.toLowerCase(),
                object,
                path
        );
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(userEmail);
        if (user.isEmpty())
            throw new UsernameNotFoundException("User not found!");
        return User.withUsername(user.get().getEmail())
                .password(user.get().getPassword())
                .accountLocked(!isAdmin(user.get()) && user.get().isBlocked())
                .authorities(getAuthorities(user.get()))
                .build();
    }

    public boolean isAdmin(UserEntity user){
        return user.getGroups().contains(getAdminGroup());
    }

    private Collection<GrantedAuthority> getAuthorities(UserEntity userEntity){
        return userEntity.getGroups()
                .stream()
                .map(group -> new SimpleGrantedAuthority(group.getName().toUpperCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void changePassword(UserEntity user, String pass, String subject, String object, String path){
        if( isPasswordBreached(pass)) {
            throw new PasswordBreachedException();
        }
        boolean isPasswordEqualsPast = encoder.matches(pass, user.getPassword());
        if (isPasswordEqualsPast) {
            throw new PasswordExistException();
        }
        user.setPassword(encoder.encode(pass));
        save(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.CHANGE_PASSWORD,
                subject.toLowerCase(),
                object.toLowerCase(),
                path
        );
    }

    public boolean isPasswordBreached(String pass){
        return breachedPasswordRepository.existsByPassword(pass);
    }

    public void lock(UserEntity user, String subject, String object, String path){
        user.setBlocked(true);
        save(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.LOCK_USER,
                subject.toLowerCase(),
                object,
                path
        );
    }

    public void unlock(UserEntity user, String subject, String object, String path){
        user.setFailedAttempt(0);
        user.setBlocked(false);
        save(user);
        eventsService.makeEvent(
                SecurityEvent.EventType.UNLOCK_USER,
                subject.toLowerCase(),
                object,
                path
        );
    }

    public List<Group> getBusinessGroups(){
        List<Group> result =  (List<Group>) groupRepository.findAll();
        result.remove(getAdminGroup());
        return result;
    }

    public Group getAdminGroup(){
        return findGroupByName("ROLE_ADMINISTRATOR");
    }
}
