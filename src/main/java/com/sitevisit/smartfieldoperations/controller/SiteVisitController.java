package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.Company;
import com.sitevisit.smartfieldoperations.entity.SiteVisit;
import com.sitevisit.smartfieldoperations.repository.CompanyRepository;
import com.sitevisit.smartfieldoperations.repository.SiteVisitRepository;
import com.sitevisit.smartfieldoperations.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.sitevisit.smartfieldoperations.service.PdfService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.util.List;
@Controller
public class SiteVisitController {

    private final SiteVisitRepository siteVisitRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final PdfService pdfService;

    public SiteVisitController(
            SiteVisitRepository siteVisitRepository,
            CompanyRepository companyRepository,
            NotificationService notificationService,
            PdfService pdfService
    ) {
        this.siteVisitRepository = siteVisitRepository;
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
        this.pdfService = pdfService;
    }

    // =========================
    // SAVE NEW SITE VISIT
    // =========================

    @PostMapping("/site-visits/save")
    public String saveSiteVisit(
            @RequestParam Long companyId,
            @RequestParam String visitDate,
            @RequestParam String visitTime,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {

        String userEmail =
                (String) session.getAttribute(
                        "loggedInUserEmail"
                );

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                ));

        SiteVisit siteVisit = new SiteVisit();

        siteVisit.setCompany(company);

        siteVisit.setVisitDate(
                java.time.LocalDate.parse(visitDate)
        );

        siteVisit.setVisitTime(
                java.time.LocalTime.parse(visitTime)
        );

        siteVisit.setStatus("Scheduled");

        siteVisit.setNotes(notes);

        siteVisit.setCheckedIn(false);

        siteVisitRepository.save(siteVisit);

        notificationService.createNotification(
                "New site visit scheduled with "
                        + company.getName()
                        + " on "
                        + siteVisit.getVisitDate()
                        + " at "
                        + siteVisit.getVisitTime(),

                "SITE_VISIT_SCHEDULED",

                "/site-visits",

                userEmail,

                "Site Visit Scheduled"
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Site visit scheduled successfully."
        );

        return "redirect:/site-visits";
    }

    // =========================
    // EDIT SITE VISIT
    // =========================

    @GetMapping("/site-visits/edit/{id}")
    public String editSiteVisit(
            @PathVariable Long id,
            Model model
    ) {

        SiteVisit siteVisit =
                siteVisitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Site visit not found"
                                ));

        model.addAttribute(
                "editVisit",
                siteVisit
        );

        model.addAttribute(
                "companies",
                companyRepository.findAll()
        );

        model.addAttribute(
                "siteVisits",
                siteVisitRepository.findAll()
        );

        return "site-visits";
    }

    // =========================
    // UPDATE SITE VISIT
    // =========================

    @PostMapping("/site-visits/update/{id}")
    public String updateSiteVisit(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @RequestParam String visitDate,
            @RequestParam String visitTime,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes
    ) {

        SiteVisit siteVisit =
                siteVisitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Site visit not found"
                                ));

        Company company =
                companyRepository.findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                ));

        siteVisit.setCompany(company);

        siteVisit.setVisitDate(
                java.time.LocalDate.parse(visitDate)
        );

        siteVisit.setVisitTime(
                java.time.LocalTime.parse(visitTime)
        );

        siteVisit.setNotes(notes);

        siteVisitRepository.save(siteVisit);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Site visit updated successfully."
        );

        return "redirect:/site-visits";
    }

    // =========================
    // DELETE SITE VISIT
    // =========================

    @PostMapping("/site-visits/delete/{id}")
    public String deleteSiteVisit(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        siteVisitRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Site visit deleted successfully."
        );

        return "redirect:/site-visits";
    }

    // =========================
    // UPDATE STATUS
    // =========================

    @PostMapping("/site-visits/update-status/{id}")
    @ResponseBody
    public java.util.Map<String, Object> updateVisitStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        java.util.Map<String, Object> response =
                new java.util.HashMap<>();

        try {

            SiteVisit visit =
                    siteVisitRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Site visit not found"
                                    ));

            visit.setStatus(status);

            if (status.equalsIgnoreCase("In Progress")
                    || status.equalsIgnoreCase("Checked In")) {

                visit.setCheckedIn(true);
            }

            siteVisitRepository.save(visit);

            response.put("success", true);

            response.put(
                    "message",
                    "Status updated successfully."
            );

        } catch (Exception e) {

            response.put("success", false);

            response.put(
                    "message",
                    "Failed to update status."
            );
        }

        return response;
    }
    // =========================
// DOWNLOAD SITE VISITS PDF
// =========================

    @GetMapping("/site-visits/download-pdf")
    public ResponseEntity<InputStreamResource> downloadPdf(){


        List<SiteVisit> visits =
                siteVisitRepository.findAll();


        ByteArrayInputStream pdf =
                pdfService.generateSiteVisitsPdf(visits);



        HttpHeaders headers =
                new HttpHeaders();


        headers.add(
                "Content-Disposition",
                "attachment; filename=scheduled-site-visits.pdf"
        );


        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(
                        new InputStreamResource(pdf)
                );

    }
}