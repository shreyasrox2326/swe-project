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
        Event event = eventRepo.findById(category.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if ("deleted".equalsIgnoreCase(event.getStatus())) {
            throw new RuntimeException("Cannot manage ticket categories for a deleted event");
        }

        // Validate quantities and price
        if (category.getTotalQty() <= 0) throw new RuntimeException("Total quantity must be positive");
        if (category.getAvailableQty() < 0 || category.getAvailableQty() > category.getTotalQty())
            throw new RuntimeException("Invalid available quantity");
        if (category.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0)
            throw new RuntimeException("Price must be zero or greater");

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

    @GetMapping("/event/{eventId}")
    public List<TicketCategory> getByEvent(@PathVariable String eventId) {
        return categoryRepo.findByEventId(eventId);
    }

    // DELETE CATEGORY
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        categoryRepo.deleteById(id);
    }
}
