package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, NotificationRepository notificationRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET all
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE
    @PostMapping
    public User createUser(@RequestBody User user) {
        if (user.getUser_id() == null || user.getUser_id().isBlank()) {
            user.setUser_id(UUID.randomUUID().toString());
        }
        if (user.getType() == null) {
            user.setType(UserType.customer);
        }
        if (user.getPassword_hash() == null || user.getPassword_hash().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (!user.getPassword_hash().startsWith("$2")) {
            user.setPassword_hash(passwordEncoder.encode(user.getPassword_hash()));
        }
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return new LoginResponse(user);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    UserType previousType = user.getType();
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPhone(updatedUser.getPhone());
                    if (updatedUser.getPassword_hash() != null && !updatedUser.getPassword_hash().isBlank()) {
                        if (updatedUser.getPassword_hash().startsWith("$2")) {
                            user.setPassword_hash(updatedUser.getPassword_hash());
                        } else {
                            user.setPassword_hash(passwordEncoder.encode(updatedUser.getPassword_hash()));
                        }
                    }
                    user.setType(updatedUser.getType());
                    User savedUser = userRepository.save(user);

                    if (previousType != savedUser.getType()) {
                        Notification notification = new Notification();
                        notification.setNotificationId(UUID.randomUUID().toString());
                        notification.setType("role_assignment");
                        notification.setMessage("Your account role has been updated to " + savedUser.getType().name() + ".");
                        notification.setUserId(savedUser.getUser_id());
                        notificationRepository.save(notification);
                    }

                    return ResponseEntity.ok(savedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
