package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.entity.PaymentReminder;
import com.sitevisit.smartfieldoperations.service.PaymentReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-reminders")
public class PaymentReminderController {

    private final PaymentReminderService paymentReminderService;

    public PaymentReminderController(PaymentReminderService paymentReminderService) {
        this.paymentReminderService = paymentReminderService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentReminder>> getAllReminders() {
        return ResponseEntity.ok(paymentReminderService.getAllReminders());
    }

    @PostMapping
    public ResponseEntity<PaymentReminder> createReminder(@RequestBody PaymentReminder reminder) {
        return ResponseEntity.ok(paymentReminderService.createReminder(reminder));
    }

    @PutMapping("/{id}/paid")
    public ResponseEntity<PaymentReminder> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(paymentReminderService.markAsPaid(id));
    }

    @PostMapping("/send-now")
    public ResponseEntity<String> sendNow() {
        paymentReminderService.checkAndSendReminders();
        return ResponseEntity.ok("Reminder check completed");
    }
    @PutMapping("/{id}")
    public ResponseEntity<PaymentReminder> updateReminder(
            @PathVariable Long id,
            @RequestBody PaymentReminder reminder) {

        return ResponseEntity.ok(paymentReminderService.updateReminder(id, reminder));
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleDuplicateReminder(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}