package com.sitevisit.smartfieldoperations.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "site_visits")
public class SiteVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Link to Company
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    private LocalDate lastReminderSentDate;
    private boolean checkedIn = false;
    private LocalDate visitDate;
    private LocalTime visitTime;

    private String status;

    @Column(length = 1000)
    private String notes;

    public SiteVisit() {
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public LocalTime getVisitTime() {
        return visitTime;
    }

    public LocalDate getLastReminderSentDate() { return lastReminderSentDate; }

    public void setLastReminderSentDate(LocalDate lastReminderSentDate) {this.lastReminderSentDate = lastReminderSentDate;}

    public boolean isCheckedIn() {return checkedIn;}

    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn;}

    public void setVisitTime(LocalTime visitTime) {
        this.visitTime = visitTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}