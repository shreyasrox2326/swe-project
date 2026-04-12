package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    Ticket findByQrCode(String qrCode);
    List<Ticket> findByBookingId(String bookingId);
}
