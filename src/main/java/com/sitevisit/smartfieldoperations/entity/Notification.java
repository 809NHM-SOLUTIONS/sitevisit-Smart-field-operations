package com.sitevisit.smartfieldoperations.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String type;

    private boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public Notification() {}

    // Constructor for creating notifications easily
    public Notification(String message, String type) {
        this.message = message;
        this.type = type;
        this.isRead = false;
    }

    // 🔥 Auto-set timestamp before saving
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}