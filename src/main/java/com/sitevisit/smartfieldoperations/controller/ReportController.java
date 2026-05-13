package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.Report;
import com.sitevisit.smartfieldoperations.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import com.sitevisit.smartfieldoperations.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080"})
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    @Autowired
    public ReportController(ReportRepository reportRepository,
                            NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
    }


    @PostMapping
    public ResponseEntity<Report> createReport(
            @RequestBody Report report,
            jakarta.servlet.http.HttpSession session
    ) {

        try {

            logger.info(
                    "📝 Creating report: title='{}'",
                    report.getTitle()
            );

            if (report.getUpdatedAt() == null) {
                report.setUpdatedAt(LocalDateTime.now());
            }

            Report saved =
                    reportRepository.save(report);

            // LOGGED-IN USER EMAIL
            String userEmail =
                    (String) session.getAttribute(
                            "loggedInUserEmail"
                    );

            // NOTIFICATION + EMAIL
            notificationService.createNotification(
                    "New report submitted: "
                            + saved.getTitle(),

                    "REPORT_SUBMITTED",

                    "/reports",

                    userEmail,

                    "New Report Submitted"
            );
            logger.info(
                    "✅ Saved report ID: {}",
                    saved.getId()
            );

            return ResponseEntity.ok(saved);

        } catch (Exception e) {

            logger.error(
                    "❌ Failed to create report",
                    e
            );

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        try { return ResponseEntity.ok(reportRepository.findAll()); } 
        catch (Exception e) { 
            logger.error("Error fetching reports", e);
            return ResponseEntity.internalServerError().build(); 
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return reportRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable Long id, @RequestBody Report details) {
        return reportRepository.findById(id)
                .map(existing -> {
                    if (details.getTitle() != null) existing.setTitle(details.getTitle());
                    if (details.getSiteVisitId() != null) existing.setSiteVisitId(details.getSiteVisitId());
                    if (details.getSummary() != null) existing.setSummary(details.getSummary());
                    if (details.getFindings() != null) existing.setFindings(details.getFindings());
                    if (details.getIssues() != null) existing.setIssues(details.getIssues());
                    if (details.getActionsTaken() != null) existing.setActionsTaken(details.getActionsTaken());
                    if (details.getRecommendations() != null) existing.setRecommendations(details.getRecommendations());
                    if (details.getNotes() != null) existing.setNotes(details.getNotes());
                    
                    existing.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(reportRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // ✅ UPDATED PDF GENERATION
    // ==========================================
    @GetMapping("/{id}/pdf")
    public void downloadReport(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));

        response.setContentType("application/pdf");
        String safeTitle = report.getTitle() != null 
            ? report.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_").substring(0, Math.min(30, report.getTitle().length()))
            : "report";
        response.setHeader("Content-Disposition", "attachment; filename=SiteVisit_Report_" + safeTitle + "_" + id + ".pdf");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        try {
            Document document = new Document(PageSize.A4, 40, 40, 30, 30);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL);

            // 1. Header
            document.add(new Paragraph("SITEVISIT FIELD REPORT", titleFont));
            document.add(new Paragraph(" "));
            
            // 2. Basic Info
            document.add(new Paragraph("Report ID: " + report.getId(), labelFont));
            document.add(new Paragraph("Title: " + report.getTitle(), valueFont));
            document.add(new Paragraph("Summary: " + report.getSummary(), valueFont));
            document.add(new Paragraph(" ")); // Space

            // 3. ✅ NEW: Add all other fields if they exist
            if (report.getFindings() != null && !report.getFindings().isEmpty()) {
                document.add(new Paragraph("Findings:", labelFont));
                document.add(new Paragraph(report.getFindings(), valueFont));
                document.add(new Paragraph(" "));
            }

            if (report.getIssues() != null && !report.getIssues().isEmpty()) {
                document.add(new Paragraph("Issues Identified:", labelFont));
                document.add(new Paragraph(report.getIssues(), valueFont));
                document.add(new Paragraph(" "));
            }

            if (report.getActionsTaken() != null && !report.getActionsTaken().isEmpty()) {
                document.add(new Paragraph("Actions Taken:", labelFont));
                document.add(new Paragraph(report.getActionsTaken(), valueFont));
                document.add(new Paragraph(" "));
            }

            if (report.getRecommendations() != null && !report.getRecommendations().isEmpty()) {
                document.add(new Paragraph("Recommendations:", labelFont));
                document.add(new Paragraph(report.getRecommendations(), valueFont));
                document.add(new Paragraph(" "));
            }

            if (report.getNotes() != null && !report.getNotes().isEmpty()) {
                document.add(new Paragraph("Additional Notes:", labelFont));
                document.add(new Paragraph(report.getNotes(), valueFont));
                document.add(new Paragraph(" "));
            }

            // 4. Footer
            document.add(new Paragraph("=".repeat(50), labelFont));
            document.add(new Paragraph("Generated: " + LocalDateTime.now(), valueFont));

            document.close();
            logger.info("✅ Generated PDF for report: {}", id);

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}