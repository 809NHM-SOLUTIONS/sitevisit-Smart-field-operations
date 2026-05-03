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

    public PaymentReminderService(PaymentReminderRepository paymentReminderRepository,
                                  EmailService emailService,
                                  NotificationService notificationService) {
        this.paymentReminderRepository = paymentReminderRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public List<PaymentReminder> getAllReminders() {
        return paymentReminderRepository.findAll();
    }

    public PaymentReminder createReminder(PaymentReminder reminder) {

        if (!paymentReminderRepository.findAll().isEmpty()) {
            throw new IllegalStateException("You already have a payment reminder. Edit it instead.");        }

        reminder.setPaid(false);

        PaymentReminder savedReminder = paymentReminderRepository.save(reminder);

        notificationService.createNotification(
                "New stipend payment reminder created: " + savedReminder.getTitle(),
                "PAYMENT_REMINDER"
        );

        return savedReminder;
    }

    public PaymentReminder markAsPaid(Long id) {
        PaymentReminder reminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment reminder not found"));

        LocalDate today = LocalDate.now();

        reminder.setPaid(false); // keep reminder active for next month
        reminder.setPaidDate(today);
        reminder.setPaymentDate(reminder.getPaymentDate().plusMonths(1));
        reminder.setLastReminderSentDate(null);

        PaymentReminder savedReminder = paymentReminderRepository.save(reminder);

        notificationService.createNotification(
                "Payment marked as paid. Next reminder scheduled for: " + savedReminder.getPaymentDate(),
                "PAYMENT_PAID"
        );

        return savedReminder;
    }

    public void checkAndSendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate fourDaysFromNow = today.plusDays(4);

        List<PaymentReminder> reminders = paymentReminderRepository.findAll();

        for (PaymentReminder reminder : reminders) {

            boolean dueSoon = !reminder.getPaymentDate().isBefore(today)
                    && !reminder.getPaymentDate().isAfter(fourDaysFromNow);

            boolean overdue = reminder.getPaymentDate().isBefore(today);

            boolean alreadySentToday = today.equals(reminder.getLastReminderSentDate());

            if (!reminder.isPaid() && (dueSoon || overdue) && !alreadySentToday) {

                String subject = overdue
                        ? "Overdue Stipend Payment Reminder"
                        : "Upcoming Stipend Payment Reminder";

                String message;

                if (overdue) {
                    message = "OVERDUE: " + reminder.getTitle()
                            + " was due on " + reminder.getPaymentDate()
                            + ". Please process the payment as soon as possible.";
                } else {
                    message = reminder.getTitle()
                            + " is due on " + reminder.getPaymentDate()
                            + ". Please ensure it is processed on time.";
                }

                if (reminder.getMessage() != null && !reminder.getMessage().isBlank()) {
                    message += "\n\nNote: " + reminder.getMessage();
                }

                emailService.sendEmail(
                        reminder.getRecipientEmail(),
                        subject,
                        message
                );

                notificationService.createNotification(
                        "Payment Reminder: " + reminder.getTitle() + " (" + reminder.getPaymentDate() + ")",
                        overdue ? "PAYMENT_OVERDUE" : "PAYMENT_REMINDER"
                );


                reminder.setLastReminderSentDate(today);
                paymentReminderRepository.save(reminder);
            }
        }
    }
    public PaymentReminder updateReminder(Long id, PaymentReminder updatedReminder) {
        PaymentReminder existingReminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment reminder not found"));

        existingReminder.setTitle(updatedReminder.getTitle());
        existingReminder.setPaymentDate(updatedReminder.getPaymentDate());
        existingReminder.setMessage(updatedReminder.getMessage());

        PaymentReminder savedReminder = paymentReminderRepository.save(existingReminder);

        notificationService.createNotification(
                "Payment reminder updated: " + savedReminder.getTitle(),
                "PAYMENT_UPDATED"
        );

        return savedReminder;
    }
}