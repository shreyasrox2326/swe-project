package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ticket-categories")
@CrossOrigin
public class TicketCategoryController {

    private final TicketCategoryRepository categoryRepo;
    private final EventRepository eventRepo;

    public TicketCategoryController(TicketCategoryRepository categoryRepo, EventRepository eventRepo) {
        this.categoryRepo = categoryRepo;
        this.eventRepo = eventRepo;
    }

    // CREATE CATEGORY
    @PostMapping
    public TicketCategory create(@RequestBody TicketCategory category) {

        // Validate event exists
        eventRepo.findById(category.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validate quantities and price
        if (category.getTotalQty() <= 0) throw new RuntimeException("Total quantity must be positive");
        if (category.getAvailableQty() < 0 || category.getAvailableQty() > category.getTotalQty())
            throw new RuntimeException("Invalid available quantity");
        if (category.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Price must be positive");

        return categoryRepo.save(category);
    }

    // GET ALL CATEGORIES
    @GetMapping
    public List<TicketCategory> getAll() {
        return categoryRepo.findAll();
    }

    // GET CATEGORY BY ID
    @GetMapping("/{id}")
    public TicketCategory getById(@PathVariable String id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // DELETE CATEGORY
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        categoryRepo.deleteById(id);
    }
}