package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal-tickets")
@CrossOrigin
public class InternalTicketController {

    private final InternalTicketService internalTicketService;

    public InternalTicketController(InternalTicketService internalTicketService) {
        this.internalTicketService = internalTicketService;
    }

    @PostMapping("/issue")
    public InternalTicketIssueResponse issue(@RequestBody InternalTicketIssueRequest request) {
        return internalTicketService.issueInternalTickets(
                request.getEventId(),
                request.getUserId(),
                request.getType(),
                request.getQuantity(),
                request.getCreatedByUserId()
        );
    }
}
