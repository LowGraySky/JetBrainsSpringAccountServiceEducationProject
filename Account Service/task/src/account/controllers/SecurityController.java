package account.controllers;

import account.services.SecurityEventsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/security")
public class SecurityController {

    private final SecurityEventsService eventService;

    public SecurityController(SecurityEventsService eventsService){
        this.eventService = eventsService;
    }

    @GetMapping("/events")
    public ResponseEntity<Object> allEvents(){
        return new ResponseEntity<>( eventService.getAll(), HttpStatus.OK);
    }
}
