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

    @GetMapping("/{reportId}")
    public Report getById(@PathVariable String reportId) {
        return reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    @PutMapping("/{reportId}")
    public Report update(@PathVariable String reportId, @RequestBody Report payload) {
        Report existing = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        existing.setGeneratedDate(payload.getGeneratedDate() != null ? payload.getGeneratedDate() : existing.getGeneratedDate());
        existing.setOrganizerId(payload.getOrganizerId() != null ? payload.getOrganizerId() : existing.getOrganizerId());
        existing.setData(payload.getData() != null ? payload.getData() : existing.getData());
        return reportRepo.save(existing);
    }

    @DeleteMapping("/{reportId}")
    public void delete(@PathVariable String reportId) {
        reportRepo.deleteById(reportId);
    }
}
