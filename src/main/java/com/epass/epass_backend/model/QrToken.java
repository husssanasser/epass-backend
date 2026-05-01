package com.epass.epass_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "qr_tokens")
public class QrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "permit_id", nullable = false)
    private Permit permit;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "is_valid")
    private Boolean isValid = true;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
}