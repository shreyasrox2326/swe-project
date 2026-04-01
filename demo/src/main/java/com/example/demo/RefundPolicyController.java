package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/refund-policies")
@CrossOrigin
public class RefundPolicyController {

    private final RefundPolicyRepository repo;

    public RefundPolicyController(RefundPolicyRepository repo) {
        this.repo = repo;
    }

    // CREATE or UPDATE a policy
    @PostMapping
    public RefundPolicy createOrUpdate(@RequestBody RefundPolicy policy) {
        return repo.save(policy);
    }

    // GET all policies
    @GetMapping
    public List<RefundPolicy> getAll() {
        return repo.findAll();
    }

    // GET policy by ID
    @GetMapping("/{id}")
    public RefundPolicy getById(@PathVariable String id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund policy not found"));
    }

    // DELETE policy
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        repo.deleteById(id);
    }
}