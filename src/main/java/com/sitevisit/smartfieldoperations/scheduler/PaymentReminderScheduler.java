package com.sitevisit.smartfieldoperations.scheduler;

import com.sitevisit.smartfieldoperations.service.PaymentReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderScheduler {

    private final PaymentReminderService paymentReminderService;

    public PaymentReminderScheduler(
            PaymentReminderService paymentReminderService
    ) {
        this.paymentReminderService =
                paymentReminderService;
    }

    // Runs every day at 08:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyReminderCheck() {

        // SYSTEM EMAIL
        String systemEmail =
                "nommypre@gmail.com";

        paymentReminderService.checkAndSendReminders(
                systemEmail
        );
    }
}