package account.services;

import account.entities.SecurityEvent;
import account.repositories.SecurityEventRepository;
import com.sun.istack.logging.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityEventsService {

    private final SecurityEventRepository repository;
    private  final Logger LOGGER = Logger.getLogger(SecurityEventsService.class);

    public SecurityEventsService(SecurityEventRepository repository){
        this.repository = repository;
    }

    public List<SecurityEvent> getAll(){
        List<SecurityEvent> result = (List<SecurityEvent>) repository.findAll();
        return result.stream()
                .sorted(Comparator.comparingLong(SecurityEvent::getId))
                .collect(Collectors.toList());
    }

    public void save(SecurityEvent event){
        repository.save(event);
    }

    public void makeEvent(SecurityEvent.EventType type, String subject, String object, String path){
        SecurityEvent event = new SecurityEvent(
                Date.from(Instant.now()),
                type,
                subject,
                object,
                path
        );
        save(event);
        LOGGER.info(event.toString());
    }
}
