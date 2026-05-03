package com.sitevisit.smartfieldoperations.repository;

import com.sitevisit.smartfieldoperations.entity.PaymentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;



public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {
    List<PaymentReminder> findByPaidFalse();
}