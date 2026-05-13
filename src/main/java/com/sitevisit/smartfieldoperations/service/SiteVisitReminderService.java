package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.entity.SiteVisit;
import com.sitevisit.smartfieldoperations.entity.User;
import com.sitevisit.smartfieldoperations.repository.SiteVisitRepository;
import com.sitevisit.smartfieldoperations.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SiteVisitReminderService {

    private final SiteVisitRepository siteVisitRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public SiteVisitReminderService(SiteVisitRepository siteVisitRepository,
                                    EmailService emailService,
                                    NotificationService notificationService,
                                    UserRepository userRepository) {
        this.siteVisitRepository = siteVisitRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Used by button/manual test - uses logged-in user email
    public void checkAndSendSiteVisitReminders(String userEmail) {
        processSiteVisitReminders(userEmail);
    }

    // Used by scheduler - fallback user
    public void checkAndSendSiteVisitReminders() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return;
        }

        String userEmail = users.get(0).getEmail();
        processSiteVisitReminders(userEmail);
    }

    private void processSiteVisitReminders(String userEmail) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<SiteVisit> visits =
                siteVisitRepository.findByVisitDateBetweenAndCheckedInFalse(today, tomorrow);

        for (SiteVisit visit : visits) {
            boolean alreadySentToday = today.equals(visit.getLastReminderSentDate());

            if (!alreadySentToday) {
                String companyName = visit.getCompany().getName();

                String subject = "Upcoming Site Visit Reminder";

                String message = "Reminder: You have a site visit scheduled with "
                        + companyName
                        + " on " + visit.getVisitDate()
                        + " at " + visit.getVisitTime()
                        + ". Please ensure you attend as planned.";

                emailService.sendEmail(userEmail, subject, message);

                notificationService.createNotification(
                        "Site Visit Reminder: " + companyName + " ("
                                + visit.getVisitDate() + " "
                                + visit.getVisitTime() + ")",

                        "SITE_VISIT_REMINDER",

                        "/site-visits/" + visit.getId(),

                        userEmail,

                        "Upcoming Site Visit Reminder"
                );

                visit.setLastReminderSentDate(today);
                siteVisitRepository.save(visit);
            }
        }
    }
}