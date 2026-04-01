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

    @PostMapping
    public Corporate create(@RequestBody Corporate corporate) {

        User user = userRepository.findById(corporate.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getType() != UserType.corporate) {
    throw new RuntimeException("User is not a corporate");
  }

        return repo.save(corporate);
    }
}
