package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reports")
@CrossOrigin
public class ReportController {

    private final ReportRepository reportRepo;

    public ReportController(ReportRepository reportRepo) {
        this.reportRepo = reportRepo;
    }

    // CREATE report
    @PostMapping
    public Report create(@RequestBody Report report) {
        return reportRepo.save(report);
    }

    // GET all reports
    @GetMapping
    public List<Report> getAll() {
        return reportRepo.findAll();
    }

    // GET reports by organizer
    @GetMapping("/organizer/{organizerId}")
    public List<Report> getByOrganizer(@PathVariable String organizerId) {
        return reportRepo.findByOrganizerId(organizerId);
    }
}