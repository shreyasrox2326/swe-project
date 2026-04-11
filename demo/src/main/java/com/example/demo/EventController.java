package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin
public class EventController {

    private final EventRepository repo;
    private final UserRepository userRepository;

    public EventController(EventRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    // CREATE EVENT
    @PostMapping
    public Event create(@RequestBody Event event) {

        // check organizer exists
        User user = userRepository.findById(event.getOrganizerId())
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        // check user is actually organizer
        if (user.getType() != UserType.organizer) {
            throw new RuntimeException("User is not an organizer");
        }

        // validate time
        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            throw new RuntimeException("Capacity must be positive");
        }

        String status = event.getStatus() == null ? "published" : event.getStatus().trim().toLowerCase(Locale.ROOT);
        if (!status.equals("published") && !status.equals("cancelled") && !status.equals("deleted")) {
            throw new RuntimeException("Unsupported event status");
        }
        event.setStatus(status);

        return repo.save(event);
    }

    // GET ALL
    @GetMapping
    public List<Event> getAll() {
        return repo.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Event getById(@PathVariable String id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public Event delete(@PathVariable String id) {
        Event event = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setStatus("deleted");
        return repo.save(event);
    }
}
