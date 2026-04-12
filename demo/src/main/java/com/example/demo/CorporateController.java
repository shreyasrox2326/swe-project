package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/corporates")
@CrossOrigin
public class CorporateController {

    private final CorporateRepository repo;
    private final UserRepository userRepository;

    public CorporateController(CorporateRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Corporate> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{userId}")
    public Corporate getByUserId(@PathVariable String userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Corporate profile not found"));
    }

    @PostMapping
    public Corporate create(@RequestBody Corporate corporate) {

        User user = userRepository.findById(corporate.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getType() != UserType.corporate) {
    throw new RuntimeException("User is not a corporate");
  }

        return repo.save(corporate);
    }

    @PutMapping("/{userId}")
    public Corporate upsert(@PathVariable String userId, @RequestBody Corporate corporate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getType() != UserType.corporate) {
            throw new RuntimeException("User is not a corporate");
        }

        Corporate profile = repo.findById(userId).orElseGet(Corporate::new);
        profile.setUserId(userId);
        profile.setCompanyName(corporate.getCompanyName());
        profile.setGstNumber(corporate.getGstNumber());
        return repo.save(profile);
    }
}
