package com.epass.epass_backend.service;

import com.epass.epass_backend.model.*;
import com.epass.epass_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private PermitRequestRepository permitRequestRepository;

    @Autowired
    private PermitRepository permitRepository;

    @Autowired
    private QrTokenRepository qrTokenRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private QRCodeService qrCodeService;

    // Get all requests
    public List<PermitRequest> getAllRequests() {
        return permitRequestRepository.findAll();
    }

    // Get pending requests only
    public List<PermitRequest> getPendingRequests() {
        return permitRequestRepository.findByStatus(PermitRequest.Status.PENDING);
    }

    // Approve request manually by admin
    public Permit approveRequest(Long requestId) throws Exception {
        PermitRequest request = permitRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(PermitRequest.Status.APPROVED);
        permitRequestRepository.save(request);

        String token = UUID.randomUUID().toString();
        String qrCodeImage = qrCodeService.generateQRCodeImage(token);

        Permit permit = new Permit();
        permit.setPermitRequest(request);
        permit.setUser(request.getUser());
        permit.setQrCode(qrCodeImage);
        permit.setExpiryDate(request.getEndDate());
        permitRepository.save(permit);

        QrToken qrToken = new QrToken();
        qrToken.setPermit(permit);
        qrToken.setToken(token);
        qrToken.setIsValid(true);
        qrToken.setExpiryDate(request.getEndDate());
        qrTokenRepository.save(qrToken);

        Notification notification = new Notification();
        notification.setUser(request.getUser());
        notification.setMessage(
                "✅ Your " + request.getPermitType() +
                        " request has been APPROVED by admin! You can now download your E-Pass.");
        notificationRepository.save(notification);

        try {
            emailService.sendApprovalEmail(
                    request.getUser().getEmail(),
                    request.getUser().getFullName()
            );
        } catch (Exception e) {
            System.out.println("Email not sent: " + e.getMessage());
        }

        AuditLog log = new AuditLog();
        log.setActorType("ADMIN");
        log.setAction("MANUAL_APPROVE");
        log.setDetails("Permit manually approved for: " + request.getUser().getEmail());
        auditLogRepository.save(log);

        return permit;
    }

    // Reject request manually by admin
    public PermitRequest rejectRequest(Long requestId) {
        PermitRequest request = permitRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(PermitRequest.Status.REJECTED);
        permitRequestRepository.save(request);

        Notification notification = new Notification();
        notification.setUser(request.getUser());
        notification.setMessage(
                "❌ Your " + request.getPermitType() +
                        " request has been REJECTED by admin.");
        notificationRepository.save(notification);

        try {
            emailService.sendRejectionEmail(
                    request.getUser().getEmail(),
                    request.getUser().getFullName()
            );
        } catch (Exception e) {
            System.out.println("Email not sent: " + e.getMessage());
        }

        AuditLog log = new AuditLog();
        log.setActorType("ADMIN");
        log.setAction("MANUAL_REJECT");
        log.setDetails("Permit manually rejected for: " + request.getUser().getEmail());
        auditLogRepository.save(log);

        return request;
    }

    // Verify QR code
    public Map<String, Object> verifyQRCode(String token) {
        Map<String, Object> result = new HashMap<>();

        QrToken qrToken = qrTokenRepository.findByToken(token).orElse(null);

        if (qrToken == null) {
            result.put("status", "INVALID");
            result.put("message", "QR Code not recognized");
            return result;
        }

        if (!qrToken.getIsValid()) {
            result.put("status", "INVALID");
            result.put("message", "QR Code has been invalidated");
            return result;
        }

        if (qrToken.getExpiryDate().isBefore(LocalDate.now())) {
            result.put("status", "EXPIRED");
            result.put("message", "QR Code has expired");
            return result;
        }

        Permit permit = qrToken.getPermit();
        PermitRequest permitRequest = permit.getPermitRequest();
        User user = permit.getUser();

        result.put("status", "VALID");
        result.put("message", "Permit is valid and active");
        result.put("permitId", permit.getId());
        result.put("applicantName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("permitType", permitRequest.getPermitType());
        result.put("purpose", permitRequest.getPurpose());
        result.put("destination", permitRequest.getDestination());
        result.put("startDate", permitRequest.getStartDate().toString());
        result.put("endDate", permitRequest.getEndDate().toString());
        result.put("issueDate", permit.getIssueDate().toString());
        result.put("expiryDate", permit.getExpiryDate().toString());

        AuditLog log = new AuditLog();
        log.setActorType("OFFICER");
        log.setAction("VERIFY_QR");
        log.setDetails("QR verified for permit: " + permit.getId());
        auditLogRepository.save(log);

        return result;
    }
}