package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizers")
@CrossOrigin
public class OrganizerController {

    private final OrganizerRepository repo;
    private final UserRepository userRepository;

    public OrganizerController(OrganizerRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Organizer> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{userId}")
    public Organizer getByUserId(@PathVariable String userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Organizer profile not found"));
    }

    @PostMapping
    public Organizer create(@RequestBody Organizer organizer) {

        User user = userRepository.findById(organizer.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getType() != UserType.organizer) {
    throw new RuntimeException("User is not an organizer");
    }

        return repo.save(organizer);
    }

    @PutMapping("/{userId}")
    public Organizer upsert(@PathVariable String userId, @RequestBody Organizer organizer) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getType() != UserType.organizer) {
            throw new RuntimeException("User is not an organizer");
        }

        Organizer profile = repo.findById(userId).orElseGet(Organizer::new);
        profile.setUserId(userId);
        profile.setOrgName(organizer.getOrgName());
        return repo.save(profile);
    }
}
