package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
public class BookingController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;

    public BookingController(BookingRepository bookingRepo, UserRepository userRepo, EventRepository eventRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
    }

    // CREATE BOOKING
    @PostMapping
    public Booking create(@RequestBody Booking booking) {

        // Validate user exists
        userRepo.findById(booking.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate event exists
        Event event = eventRepo.findById(booking.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!"published".equalsIgnoreCase(event.getStatus())) {
            throw new RuntimeException("Tickets can only be booked for published events");
        }

        // Quantity and cost should be positive
        if (booking.getQuantity() <= 0) throw new RuntimeException("Quantity must be positive");
        if (booking.getTotalCost().compareTo(java.math.BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Total cost must be positive");

        return bookingRepo.save(booking);
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAll() {
        return bookingRepo.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Booking getById(@PathVariable String id) {
        return bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // DELETE BOOKING
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        bookingRepo.deleteById(id);
    }
}
