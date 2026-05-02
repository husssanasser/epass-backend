package com.epass.epass_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "permit_requests")
@Data
public class PermitRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String permitType;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private String destination;
    private String documentUrl;

    // حقول جديدة
    private String requestTime;
    private Integer durationHours;
    private String caseType;
    private String institutionName;
    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}