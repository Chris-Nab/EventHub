package au.edu.rmit.sept.webapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import au.edu.rmit.sept.webapp.model.RSVP;
import au.edu.rmit.sept.webapp.repository.RsvpRepository;
import au.edu.rmit.sept.webapp.model.Event;
import org.springframework.web.bind.annotation.ResponseBody;
@Service
public class RSVPService {

    private final RsvpRepository rsvpRepository;

    public RSVPService(RsvpRepository rsvpRepository) {
        this.rsvpRepository = rsvpRepository;
    }

    public boolean submitRSVP(Long userId, Long eventId) {
        if (rsvpRepository.checkUserAlreadyRsvped(userId, eventId)) { //this line will rarely run....
            return false;
        }
        RSVP rsvp = new RSVP();
        rsvp.setUserId(userId);
        rsvp.setEventId(eventId);
        rsvp.setCreatedAt(LocalDateTime.now());
        return rsvpRepository.save(rsvp);
    }

    public RSVP getRSVP(Long userId, Long eventId) {
        return rsvpRepository.findByUserIdAndEventId(userId, eventId); 
    }

    public boolean deleteRsvp(Long userId, Long eventId) {
        return rsvpRepository.removeRSVPbyID(userId, eventId);
    }

    public boolean deleteRsvpByEvent(Long eventId) {
        return rsvpRepository.removeRSVPbyEvent(eventId);
    }

    public List<RsvpRepository.AttendeeRow> getAllAttendeesForEvent(Long eventId) {
      return rsvpRepository.findAttendeesByEvent(eventId);
    }

    public List<Event> getRsvpedEventsByUser(Long userId, String order){
        return rsvpRepository.findEventsByUserId(userId, order);
    }
}
