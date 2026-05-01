package com.epass.epass_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "permit_requests")
public class PermitRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "permit_type", nullable = false)
    private String permitType;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "document_url")
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}