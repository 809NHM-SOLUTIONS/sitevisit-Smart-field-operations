package com.sitevisit.smartfieldoperations.repository;

import com.sitevisit.smartfieldoperations.entity.SiteVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SiteVisitRepository extends JpaRepository<SiteVisit, Long> {

    List<SiteVisit> findByVisitDateBetweenAndCheckedInFalse(
            LocalDate startDate,
            LocalDate endDate
    );
}