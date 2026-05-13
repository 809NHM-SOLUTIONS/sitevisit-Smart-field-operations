package com.sitevisit.smartfieldoperations.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate paymentDate;

    @Column(length = 1000)
    private String message;

    private boolean paid;

    private LocalDate paidDate;

    private LocalDate lastReminderSentDate;

    // ✅ ADD THIS
    private String email;

    // GETTERS + SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public LocalDate getLastReminderSentDate() {
        return lastReminderSentDate;
    }

    public void setLastReminderSentDate(LocalDate lastReminderSentDate) {
        this.lastReminderSentDate = lastReminderSentDate;
    }

    // ✅ EMAIL GETTER/SETTER

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}