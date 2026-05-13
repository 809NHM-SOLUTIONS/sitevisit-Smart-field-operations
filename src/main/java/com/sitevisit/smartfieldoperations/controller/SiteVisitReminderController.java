package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.SiteVisit;
import com.sitevisit.smartfieldoperations.repository.SiteVisitRepository;
import com.sitevisit.smartfieldoperations.service.SiteVisitReminderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/site-visit-reminders")
public class SiteVisitReminderController {

    private final SiteVisitRepository siteVisitRepository;
    private final SiteVisitReminderService siteVisitReminderService;

    public SiteVisitReminderController(
            SiteVisitRepository siteVisitRepository,
            SiteVisitReminderService siteVisitReminderService
    ) {
        this.siteVisitRepository = siteVisitRepository;
        this.siteVisitReminderService = siteVisitReminderService;
    }

    @GetMapping
    public ResponseEntity<List<SiteVisit>>
    getUpcomingSiteVisitReminders() {

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        return ResponseEntity.ok(
                siteVisitRepository
                        .findByVisitDateBetweenAndCheckedInFalse(
                                today,
                                tomorrow
                        )
        );
    }

    @PostMapping("/send-now")
    public ResponseEntity<String> sendNow(
            HttpSession session
    ) {

        String email =
                (String) session.getAttribute(
                        "loggedInUserEmail"
                );

        if (email == null || email.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("No logged-in user email found.");
        }

        siteVisitReminderService
                .checkAndSendSiteVisitReminders(email);

        return ResponseEntity.ok(
                "Site visit reminder emails sent."
        );
    }
}