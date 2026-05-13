package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.entity.PaymentReminder;
import com.sitevisit.smartfieldoperations.repository.PaymentReminderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentReminderService {

    private final PaymentReminderRepository paymentReminderRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public PaymentReminderService(
            PaymentReminderRepository paymentReminderRepository,
            EmailService emailService,
            NotificationService notificationService
    ) {
        this.paymentReminderRepository = paymentReminderRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    // GET ALL REMINDERS
    public List<PaymentReminder> getAllReminders() {
        return paymentReminderRepository.findAll();
    }

    // CREATE REMINDER
    public PaymentReminder createReminder(
            PaymentReminder reminder,
            String userEmail
    ) {

        if (!paymentReminderRepository.findAll().isEmpty()) {
            throw new IllegalStateException(
                    "You already have a payment reminder. Edit it instead."
            );
        }

        reminder.setPaid(false);

        PaymentReminder savedReminder =
                paymentReminderRepository.save(reminder);

        // EMAIL
        emailService.sendEmail(
                userEmail,
                "Payment Reminder Created",
                "A new payment reminder was created.\n\n"
                        + "Title: "
                        + savedReminder.getTitle()
                        + "\nPayment Date: "
                        + savedReminder.getPaymentDate()
        );

        // NOTIFICATION
        notificationService.createNotification(
                "New stipend payment reminder created: "
                        + savedReminder.getTitle(),
                "PAYMENT_REMINDER",
                "/reminders-notifications",
                userEmail,
                "Payment Reminder Created"
        );

        return savedReminder;
    }

    // MARK AS PAID
    public PaymentReminder markAsPaid(
            Long id,
            String userEmail
    ) {

        PaymentReminder reminder =
                paymentReminderRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment reminder not found"
                                ));

        LocalDate today = LocalDate.now();

        reminder.setPaid(true);
        reminder.setPaidDate(today);

        reminder.setPaymentDate(
                reminder.getPaymentDate().plusMonths(1)
        );

        reminder.setLastReminderSentDate(null);

        PaymentReminder savedReminder =
                paymentReminderRepository.save(reminder);

        // EMAIL
        emailService.sendEmail(
                userEmail,
                "Payment Received",
                "Payment marked as paid.\n\n"
                        + "Next payment reminder date: "
                        + savedReminder.getPaymentDate()
        );

        // NOTIFICATION
        notificationService.createNotification(
                "Payment marked as paid. Next reminder scheduled for: "
                        + savedReminder.getPaymentDate(),
                "PAYMENT_PAID",
                "/reminders-notifications",
                userEmail,
                "Payment Received"
        );

        return savedReminder;
    }

    // CHECK + SEND REMINDERS
    public void checkAndSendReminders(
            String userEmail
    ) {

        LocalDate today = LocalDate.now();
        LocalDate fourDaysFromNow =
                today.plusDays(4);

        List<PaymentReminder> reminders =
                paymentReminderRepository.findAll();

        for (PaymentReminder reminder : reminders) {

            try {

                boolean dueSoon =
                        !reminder.getPaymentDate().isBefore(today)
                                && !reminder.getPaymentDate()
                                .isAfter(fourDaysFromNow);

                boolean overdue =
                        reminder.getPaymentDate()
                                .isBefore(today);

                boolean alreadySentToday =
                        today.equals(
                                reminder.getLastReminderSentDate()
                        );

                if (!reminder.isPaid()
                        && (dueSoon || overdue)
                        && !alreadySentToday) {

                    String subject = overdue
                            ? "Overdue Stipend Payment Reminder"
                            : "Upcoming Stipend Payment Reminder";

                    String message;

                    if (overdue) {

                        message =
                                "OVERDUE: "
                                        + reminder.getTitle()
                                        + " was due on "
                                        + reminder.getPaymentDate()
                                        + ". Please process payment immediately.";

                    } else {

                        message =
                                reminder.getTitle()
                                        + " is due on "
                                        + reminder.getPaymentDate()
                                        + ". Please ensure payment is processed.";
                    }

                    if (reminder.getMessage() != null
                            && !reminder.getMessage().isBlank()) {

                        message +=
                                "\n\nNote: "
                                        + reminder.getMessage();
                    }

                    // EMAIL
                    emailService.sendEmail(
                            userEmail,
                            subject,
                            message
                    );

                    // NOTIFICATION
                    notificationService.createNotification(
                            "Payment Reminder: "
                                    + reminder.getTitle()
                                    + " ("
                                    + reminder.getPaymentDate()
                                    + ")",
                            overdue
                                    ? "PAYMENT_OVERDUE"
                                    : "PAYMENT_REMINDER",
                            "/reminders-notifications",
                            userEmail,
                            subject
                    );

                    reminder.setLastReminderSentDate(today);

                    paymentReminderRepository.save(reminder);
                }

            } catch (Exception e) {

                System.err.println(
                        "Reminder processing failed: "
                                + e.getMessage()
                );
            }
        }
    }

    // UPDATE REMINDER
    public PaymentReminder updateReminder(
            Long id,
            PaymentReminder updatedReminder,
            String userEmail
    ) {

        PaymentReminder existingReminder =
                paymentReminderRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment reminder not found"
                                ));

        existingReminder.setTitle(
                updatedReminder.getTitle()
        );

        existingReminder.setPaymentDate(
                updatedReminder.getPaymentDate()
        );

        existingReminder.setMessage(
                updatedReminder.getMessage()
        );

        PaymentReminder savedReminder =
                paymentReminderRepository.save(existingReminder);

        // EMAIL
        emailService.sendEmail(
                userEmail,
                "Payment Reminder Updated",
                "Your payment reminder has been updated.\n\n"
                        + "New payment date: "
                        + savedReminder.getPaymentDate()
        );

        // NOTIFICATION
        notificationService.createNotification(
                "Payment reminder updated: "
                        + savedReminder.getTitle(),
                "PAYMENT_UPDATED",
                "/reminders-notifications",
                userEmail,
                "Payment Reminder Updated"
        );

        return savedReminder;
    }
}