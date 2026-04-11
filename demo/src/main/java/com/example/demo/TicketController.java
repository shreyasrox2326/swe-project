package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@CrossOrigin
public class TicketController {

    private final TicketRepository ticketRepo;
    private final BookingRepository bookingRepo;
    private final TicketCategoryRepository categoryRepo;

    public TicketController(TicketRepository ticketRepo,
                            BookingRepository bookingRepo,
                            TicketCategoryRepository categoryRepo) {
        this.ticketRepo = ticketRepo;
        this.bookingRepo = bookingRepo;
        this.categoryRepo = categoryRepo;
    }

    // CREATE TICKET
@PostMapping
    public Ticket create(@RequestBody Ticket ticket) {

        // Fetch booking
        Booking booking = bookingRepo.findById(ticket.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Fetch category
        TicketCategory category = categoryRepo.findById(ticket.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));

        // Validate that the category belongs to the same event as the booking
        if (!category.getEventId().equals(booking.getEventId())) {
            throw new RuntimeException("Ticket category's event does not match booking's event");
        }

        // Status should default to BOOKED if null
        if (ticket.getStatus() == null)
            ticket.setStatus(TicketStatus.booked);

        return ticketRepo.save(ticket);
    }

    // GET ALL TICKETS
    @GetMapping
    public List<Ticket> getAll() {
        return ticketRepo.findAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Ticket getById(@PathVariable String id) {
        return ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    // UPDATE STATUS
    @PatchMapping("/{id}/status")
    public Ticket updateStatus(@PathVariable String id, @RequestParam TicketStatus status) {
        Ticket ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        return ticketRepo.save(ticket);
    }

    // DELETE TICKET
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        ticketRepo.deleteById(id);
    }
}