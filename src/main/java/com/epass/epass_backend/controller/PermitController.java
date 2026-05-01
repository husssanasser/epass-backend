package com.epass.epass_backend.controller;

import com.epass.epass_backend.dto.PermitRequestDTO;
import com.epass.epass_backend.model.Permit;
import com.epass.epass_backend.model.PermitRequest;
import com.epass.epass_backend.service.PermitService;
import com.epass.epass_backend.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permits")
@CrossOrigin(origins = "http://localhost:3000")
public class PermitController {

    @Autowired
    private PermitService permitService;

    @Autowired
    private PdfService pdfService;

    // Submit new permit request
    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(
            @RequestBody PermitRequestDTO dto,
            @AuthenticationPrincipal String email
    ) {
        try {
            PermitRequest request = permitService.submitRequest(dto, email);
            return ResponseEntity.ok(Map.of(
                    "message", "Permit request submitted successfully",
                    "requestId", request.getId(),
                    "status", request.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all requests for logged in user
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(
            @AuthenticationPrincipal String email
    ) {
        try {
            List<PermitRequest> requests = permitService.getUserRequests(email);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all approved permits for logged in user
    @GetMapping("/my-permits")
    public ResponseEntity<?> getMyPermits(
            @AuthenticationPrincipal String email
    ) {
        try {
            List<Permit> permits = permitService.getUserPermits(email);
            return ResponseEntity.ok(permits);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Download permit as PDF
    @GetMapping("/download/{permitId}")
    public ResponseEntity<?> downloadPermit(
            @PathVariable Long permitId
    ) {
        try {
            Permit permit = permitService.getPermitById(permitId);
            byte[] pdfBytes = pdfService.generatePermitPdf(permit);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "epass-permit-" + permitId + ".pdf"
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get notifications for logged in user
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal String email
    ) {
        try {
            return ResponseEntity.ok(permitService.getUserNotifications(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}