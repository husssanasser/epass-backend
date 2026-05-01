package com.epass.epass_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @Column(name = "email", unique = true, nullable = false)
        private String email;

        @Column(name = "password", nullable = false)
        private String password;

        @Column(name = "date_of_birth")
        private LocalDate dateOfBirth;

        @Enumerated(EnumType.STRING)
        @Column(name = "role")
        private Role role = Role.USER;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        public enum Role {
            USER, ADMIN, OFFICER
        }
    }

