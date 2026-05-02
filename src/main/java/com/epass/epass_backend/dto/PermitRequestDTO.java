package com.epass.epass_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PermitRequestDTO {
    private String permitType;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destination;
    private String documentUrl;

    // حقول جديدة
    private String requestTime;      // وقت الطلب (HH:mm)
    private Integer durationHours;   // مدة الطلب بالساعات
    private String caseType;         // للصحة: موعد / طارئ
    private String institutionName;  // للتعليم: اسم الجهة
    private String reason;           // سبب مختصر
}