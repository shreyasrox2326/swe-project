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
    private final BulkActionService bulkActionService;

    public BookingController(BookingRepository bookingRepo, UserRepository userRepo, EventRepository eventRepo, BulkActionService bulkActionService) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
        this.bulkActionService = bulkActionService;
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
        if (booking.getTotalCost().compareTo(java.math.BigDecimal.ZERO) < 0)
            throw new RuntimeException("Total cost must be zero or greater");

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

    @GetMapping("/user/{userId}")
    public List<Booking> getByUser(@PathVariable String userId) {
        return bookingRepo.findByUserId(userId);
    }

    @GetMapping("/event/{eventId}")
    public List<Booking> getByEvent(@PathVariable String eventId) {
        return bookingRepo.findByEventId(eventId);
    }

    // DELETE BOOKING
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        bookingRepo.deleteById(id);
    }

    @PostMapping("/{id}/cancel")
    public BulkActionResult cancel(@PathVariable String id) {
        return bulkActionService.cancelBooking(id);
    }
}
