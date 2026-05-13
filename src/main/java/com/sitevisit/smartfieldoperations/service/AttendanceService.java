package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.dto.ApiResponse;
import com.sitevisit.smartfieldoperations.entity.Attendance;
import com.sitevisit.smartfieldoperations.entity.SiteVisit;
import com.sitevisit.smartfieldoperations.repository.AttendanceRepository;
import com.sitevisit.smartfieldoperations.repository.SiteVisitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SiteVisitRepository siteVisitRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             SiteVisitRepository siteVisitRepository) {
        this.attendanceRepository = attendanceRepository;
        this.siteVisitRepository = siteVisitRepository;
    }

    public ApiResponse checkIn(Long siteVisitId) {
        Optional<SiteVisit> optionalVisit = siteVisitRepository.findById(siteVisitId);

        if (optionalVisit.isEmpty()) {
            return new ApiResponse(false, "Scheduled site visit not found.");
        }

        SiteVisit siteVisit = optionalVisit.get();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // ✅ ONLY keep important validation
        Optional<Attendance> existingAttendance = attendanceRepository.findBySiteVisitId(siteVisitId);
        if (existingAttendance.isPresent()) {
            return new ApiResponse(false, "Already checked in.");
        }

        Attendance attendance = new Attendance();
        attendance.setSiteVisitId(siteVisitId);
        attendance.setDate(today);
        attendance.setCheckInTime(now);
        attendance.setStatus("Checked In");

        attendanceRepository.save(attendance);

        siteVisit.setStatus("In Progress");
        siteVisitRepository.save(siteVisit);

        return new ApiResponse(true, "Check-in recorded successfully.");
    }

    public ApiResponse checkOut(Long siteVisitId) {
        Optional<Attendance> optionalAttendance = attendanceRepository.findBySiteVisitId(siteVisitId);

        if (optionalAttendance.isEmpty()) {
            return new ApiResponse(false, "You must check in first.");
        }

        Attendance attendance = optionalAttendance.get();

        if (attendance.getCheckOutTime() != null) {
            return new ApiResponse(false, "Already checked out.");
        }

        SiteVisit visit = siteVisitRepository.findById(siteVisitId)
                .orElseThrow(() -> new RuntimeException("Site visit not found"));

        // ❌ REMOVED DATE RESTRICTION HERE

        attendance.setCheckOutTime(LocalTime.now());
        attendance.setStatus("Completed");
        attendanceRepository.save(attendance);

        visit.setStatus("Completed");
        siteVisitRepository.save(visit);

        return new ApiResponse(true, "Check-out recorded successfully.");
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceBySiteVisitId(Long siteVisitId) {
        return attendanceRepository.findBySiteVisitId(siteVisitId);
    }
}