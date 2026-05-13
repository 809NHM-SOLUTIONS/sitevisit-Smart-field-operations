package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.entity.Notification;
import com.sitevisit.smartfieldoperations.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;

    public NotificationService(
            NotificationRepository repository,
            EmailService emailService
    ) {
        this.repository = repository;
        this.emailService = emailService;
    }

    // newest first
    public List<Notification> getAllNotifications() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    // CREATE NOTIFICATION + SEND EMAIL
    public void createNotification(
            String message,
            String type,
            String link,
            String userEmail,
            String emailSubject
    ) {

        Notification notification = new Notification();

        notification.setMessage(message);
        notification.setType(type);
        notification.setLink(link);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        repository.save(notification);

        // SEND EMAIL
        if (userEmail != null && !userEmail.isBlank()) {

            emailService.sendEmail(
                    userEmail,
                    emailSubject,
                    message
            );
        }
    }

    public void markAsRead(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        notification.setRead(true);

        repository.save(notification);
    }
}