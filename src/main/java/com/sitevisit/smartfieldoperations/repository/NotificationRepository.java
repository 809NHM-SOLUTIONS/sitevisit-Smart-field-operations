package com.sitevisit.smartfieldoperations.repository;

import com.sitevisit.smartfieldoperations.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ✅ Returns newest notifications first
    List<Notification> findAllByOrderByCreatedAtDesc();
}