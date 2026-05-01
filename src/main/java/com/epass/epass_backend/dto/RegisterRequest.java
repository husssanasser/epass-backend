package com.epass.epass_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private LocalDate dateOfBirth;
}