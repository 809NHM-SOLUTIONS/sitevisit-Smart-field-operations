package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.entity.Notification;
import com.sitevisit.smartfieldoperations.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // CREATE notification
    public Notification createNotification(String message, String type) {
        Notification notification = new Notification(message, type);
        return notificationRepository.save(notification);
    }

    // GET all notifications (LATEST FIRST 🔥)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    // MARK AS READ
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}