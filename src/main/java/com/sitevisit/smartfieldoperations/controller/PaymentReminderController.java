package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.PaymentReminder;
import com.sitevisit.smartfieldoperations.service.PaymentReminderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-reminders")
public class PaymentReminderController {

    private final PaymentReminderService paymentReminderService;

    public PaymentReminderController(
            PaymentReminderService paymentReminderService
    ) {
        this.paymentReminderService = paymentReminderService;
    }

    // GET ALL REMINDERS
    @GetMapping
    public ResponseEntity<List<PaymentReminder>> getAllReminders() {

        return ResponseEntity.ok(
                paymentReminderService.getAllReminders()
        );
    }

    // CREATE REMINDER
    @PostMapping
    public ResponseEntity<PaymentReminder> createReminder(
            @RequestBody PaymentReminder reminder,
            HttpSession session
    ) {

        String email =
                (String) session.getAttribute(
                        "loggedInUserEmail"
                );

        return ResponseEntity.ok(
                paymentReminderService.createReminder(
                        reminder,
                        email
                )
        );
    }

    // MARK AS PAID
    @PutMapping("/{id}/paid")
    public ResponseEntity<PaymentReminder> markAsPaid(
            @PathVariable Long id,
            HttpSession session
    ) {

        String email =
                (String) session.getAttribute(
                        "loggedInUserEmail"
                );

        return ResponseEntity.ok(
                paymentReminderService.markAsPaid(
                        id,
                        email
                )
        );
    }

    // UPDATE REMINDER
    @PutMapping("/{id}")
    public ResponseEntity<PaymentReminder> updateReminder(
            @PathVariable Long id,
            @RequestBody PaymentReminder reminder,
            HttpSession session
    ) {

        String email =
                (String) session.getAttribute(
                        "loggedInUserEmail"
                );

        return ResponseEntity.ok(
                paymentReminderService.updateReminder(
                        id,
                        reminder,
                        email
                )
        );
    }

    // SEND CHECK NOW
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

        paymentReminderService.checkAndSendReminders(
                email
        );

        return ResponseEntity.ok(
                "Reminder emails sent successfully."
        );
    }

    // HANDLE DUPLICATE REMINDER
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleDuplicateReminder(
            IllegalStateException ex
    ) {

        return ResponseEntity.badRequest()
                .body(ex.getMessage());
    }
}