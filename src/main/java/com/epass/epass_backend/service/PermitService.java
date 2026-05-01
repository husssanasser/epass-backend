package com.epass.epass_backend.service;

import com.epass.epass_backend.dto.PermitRequestDTO;
import com.epass.epass_backend.model.*;
import com.epass.epass_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PermitService {

    @Autowired
    private PermitRequestRepository permitRequestRepository;

    @Autowired
    private PermitRepository permitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AdminService adminService;

    // Submit new permit request
    public PermitRequest submitRequest(PermitRequestDTO dto, String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PermitRequest request = new PermitRequest();
        request.setUser(user);
        request.setPermitType(dto.getPermitType());
        request.setPurpose(dto.getPurpose());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setDestination(dto.getDestination());
        request.setDocumentUrl(dto.getDocumentUrl());

        // AI Auto Evaluation
        PermitRequest.Status status = autoEvaluate(dto);
        request.setStatus(status);

        permitRequestRepository.save(request);

        // Save audit log
        AuditLog log = new AuditLog();
        log.setActorType("SYSTEM");
        log.setActorId(user.getId());
        log.setAction("AUTO_EVALUATE");
        log.setDetails("Permit auto-evaluated as: " + status + " for: " + email);
        auditLogRepository.save(log);

        // If approved automatically generate permit
        if (status == PermitRequest.Status.APPROVED) {
            adminService.approveRequest(request.getId());

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("Your permit request has been automatically APPROVED!");
            notificationRepository.save(notification);
        }

        // If rejected send notification
        if (status == PermitRequest.Status.REJECTED) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("Your permit request has been automatically REJECTED. Reason: Does not meet requirements.");
            notificationRepository.save(notification);
        }

        // If pending notify user
        if (status == PermitRequest.Status.PENDING) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("Your permit request is under review by our admin team.");
            notificationRepository.save(notification);
        }

        return request;
    }

    // AI Auto Evaluation Logic
    private PermitRequest.Status autoEvaluate(PermitRequestDTO dto) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        String purpose = dto.getPurpose();
        String permitType = dto.getPermitType();
        String destination = dto.getDestination();

        long durationDays = ChronoUnit.DAYS.between(startDate, endDate);

        // ❌ REJECT — General rules for all types
        if (startDate.isBefore(today)) {
            return PermitRequest.Status.REJECTED;
        }

        if (purpose == null || purpose.trim().length() < 5) {
            return PermitRequest.Status.REJECTED;
        }

        if (destination == null || destination.trim().isEmpty()) {
            return PermitRequest.Status.REJECTED;
        }

        if (endDate.isBefore(startDate)) {
            return PermitRequest.Status.REJECTED;
        }

        // Evaluate based on permit type
        switch (permitType) {

            case "Emergency Permit":
                if (durationDays <= 3) {
                    return PermitRequest.Status.APPROVED;
                } else if (durationDays <= 7) {
                    return PermitRequest.Status.PENDING;
                } else {
                    return PermitRequest.Status.REJECTED;
                }

            case "Health Permit":
                if (durationDays > 30) {
                    return PermitRequest.Status.REJECTED;
                } else if (durationDays <= 14 && purpose.trim().length() >= 10) {
                    return PermitRequest.Status.APPROVED;
                } else {
                    return PermitRequest.Status.PENDING;
                }

            case "Work Permit":
                if (durationDays > 60) {
                    return PermitRequest.Status.REJECTED;
                } else if (durationDays <= 30 && purpose.trim().length() >= 10) {
                    return PermitRequest.Status.APPROVED;
                } else {
                    return PermitRequest.Status.PENDING;
                }

            case "Movement Permit":
                if (durationDays > 14) {
                    return PermitRequest.Status.REJECTED;
                } else if (durationDays <= 7 && purpose.trim().length() >= 10) {
                    return PermitRequest.Status.APPROVED;
                } else {
                    return PermitRequest.Status.PENDING;
                }

            default:
                return PermitRequest.Status.PENDING;
        }
    }

    // Get all requests for a user
    public List<PermitRequest> getUserRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return permitRequestRepository.findByUserId(user.getId());
    }

    // Get all approved permits for a user
    public List<Permit> getUserPermits(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return permitRepository.findByUserId(user.getId());
    }

    // Get permit by ID
    public Permit getPermitById(Long permitId) {
        return permitRepository.findById(permitId)
                .orElseThrow(() -> new RuntimeException("Permit not found"));
    }

    // Get user notifications
    public List<Notification> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserId(user.getId());
    }
}