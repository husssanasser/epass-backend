package com.epass.epass_backend.service;

import com.epass.epass_backend.dto.PermitRequestDTO;
import com.epass.epass_backend.model.*;
import com.epass.epass_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PermitService {

    @Autowired private PermitRequestRepository permitRequestRepository;
    @Autowired private PermitRepository permitRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private QrTokenRepository qrTokenRepository;
    @Autowired private QRCodeService qrCodeService;
    @Autowired private EmailService emailService;

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
        request.setRequestTime(dto.getRequestTime());
        request.setDurationHours(dto.getDurationHours());
        request.setCaseType(dto.getCaseType());
        request.setInstitutionName(dto.getInstitutionName());
        request.setReason(dto.getReason());

        PermitRequest.Status status = autoEvaluate(dto, user);
        request.setStatus(status);
        permitRequestRepository.save(request);

        AuditLog log = new AuditLog();
        log.setActorType("SYSTEM");
        log.setActorId(user.getId());
        log.setAction("AUTO_EVALUATE");
        log.setDetails("Permit auto-evaluated as: " + status + " for: " + email);
        auditLogRepository.save(log);

        if (status == PermitRequest.Status.APPROVED) {
            generatePermit(request, user);
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("✅ Your " + dto.getPermitType() + " request has been automatically APPROVED!");
            notificationRepository.save(notification);
            new Thread(() -> {
                try { emailService.sendApprovalEmail(user.getEmail(), user.getFullName()); }
                catch (Exception e) { System.out.println("Email not sent: " + e.getMessage()); }
            }).start();
        }

        if (status == PermitRequest.Status.PENDING) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("⏳ Your " + dto.getPermitType() + " request is under admin review.");
            notificationRepository.save(notification);
        }

        if (status == PermitRequest.Status.REJECTED) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("❌ Your " + dto.getPermitType() + " request has been automatically REJECTED.");
            notificationRepository.save(notification);
        }

        return request;
    }

    private void generatePermit(PermitRequest request, User user) throws Exception {
        String token = UUID.randomUUID().toString();
        String qrCodeImage = qrCodeService.generateQRCodeImage(token);

        Permit permit = new Permit();
        permit.setPermitRequest(request);
        permit.setUser(user);
        permit.setQrCode(qrCodeImage);
        permit.setExpiryDate(request.getEndDate());
        permitRepository.save(permit);

        QrToken qrToken = new QrToken();
        qrToken.setPermit(permit);
        qrToken.setToken(token);
        qrToken.setIsValid(true);
        qrToken.setExpiryDate(request.getEndDate());
        qrTokenRepository.save(qrToken);
    }

    private PermitRequest.Status autoEvaluate(PermitRequestDTO dto, User user) {
        String permitType = dto.getPermitType();
        String destination = dto.getDestination();
        String attachment = dto.getDocumentUrl();
        String caseType = dto.getCaseType();
        String reason = dto.getReason();
        Integer duration = dto.getDurationHours();
        LocalDate startDate = dto.getStartDate();
        LocalDate today = LocalDate.now();

        // رفض عام — تاريخ في الماضي
        if (startDate == null || startDate.isBefore(today)) {
            return PermitRequest.Status.REJECTED;
        }

        switch (permitType) {

            case "Work":
                if (duration == null || duration <= 0) return PermitRequest.Status.REJECTED;
                if (duration > 12) return PermitRequest.Status.REJECTED;
                if (reason == null || reason.trim().length() < 3) return PermitRequest.Status.REJECTED;
                if (duration <= 8 && reason.trim().length() >= 3) return PermitRequest.Status.APPROVED;
                return PermitRequest.Status.PENDING;

            case "Health":
                if ("emergency".equalsIgnoreCase(caseType)) {
                    // الطوارئ — لازم الوجهة تكون مستشفى أو طوارئ
                    if (destination != null && isHospitalDestination(destination)) {
                        return PermitRequest.Status.APPROVED;
                    }
                    return PermitRequest.Status.REJECTED;
                }
                if ("appointment".equalsIgnoreCase(caseType)) {
                    // الموعد — لازم مرفق → PENDING للأدمن
                    if (attachment == null || attachment.trim().isEmpty()) {
                        return PermitRequest.Status.REJECTED;
                    }
                    return PermitRequest.Status.PENDING;
                }
                return PermitRequest.Status.PENDING;

            case "Education":
                // لازم مرفق (إشعار اختبار) → PENDING للأدمن
                if (attachment == null || attachment.trim().isEmpty()) {
                    return PermitRequest.Status.REJECTED;
                }
                return PermitRequest.Status.PENDING;

            case "Essential Needs":
                // نحسب كم مرة قدّم هذا الأسبوع
                int weeklyCount = getEssentialNeedsCountThisWeek(user);
                if (weeklyCount == 0) return PermitRequest.Status.APPROVED;
                if (weeklyCount == 1) return PermitRequest.Status.PENDING;
                return PermitRequest.Status.REJECTED;

            case "Travel":
                // لازم مرفق → PENDING للأدمن
                if (attachment == null || attachment.trim().isEmpty()) {
                    return PermitRequest.Status.REJECTED;
                }
                return PermitRequest.Status.PENDING;

            default:
                return PermitRequest.Status.PENDING;
        }
    }

    // تحقق إذا الوجهة مستشفى أو طوارئ
    private boolean isHospitalDestination(String destination) {
        String dest = destination.toLowerCase();
        return dest.contains("hospital") ||
                dest.contains("emergency") ||
                dest.contains("مستشفى") ||
                dest.contains("طوارئ") ||
                dest.contains("clinic") ||
                dest.contains("عيادة");
    }

    // عدد طلبات Essential Needs هذا الأسبوع
    private int getEssentialNeedsCountThisWeek(User user) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        int currentYear = LocalDate.now().getYear();

        List<PermitRequest> userRequests = permitRequestRepository.findByUserId(user.getId());
        return (int) userRequests.stream()
                .filter(r -> r.getPermitType().equals("Essential Needs"))
                .filter(r -> r.getSubmittedAt() != null)
                .filter(r -> {
                    LocalDate submittedDate = r.getSubmittedAt().toLocalDate();
                    int reqWeek = submittedDate.get(weekFields.weekOfWeekBasedYear());
                    int reqYear = submittedDate.getYear();
                    return reqWeek == currentWeek && reqYear == currentYear;
                })
                .count();
    }

    public List<PermitRequest> getUserRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return permitRequestRepository.findByUserId(user.getId());
    }

    public List<Permit> getUserPermits(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return permitRepository.findByUserId(user.getId());
    }

    public Permit getPermitById(Long permitId) {
        return permitRepository.findById(permitId)
                .orElseThrow(() -> new RuntimeException("Permit not found"));
    }

    public List<Notification> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserId(user.getId());
    }
}