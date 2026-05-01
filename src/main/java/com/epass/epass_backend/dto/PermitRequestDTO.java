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
}