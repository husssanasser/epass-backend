package com.epass.epass_backend.controller;

import com.epass.epass_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Get all permit requests
    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        try {
            return ResponseEntity.ok(adminService.getAllRequests());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get pending requests only
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests() {
        try {
            return ResponseEntity.ok(adminService.getPendingRequests());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Approve request
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.approveRequest(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Reject request
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.rejectRequest(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Verify QR code (for officers)
    @GetMapping("/verify/{token}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public ResponseEntity<?> verifyQRCode(@PathVariable String token) {
        try {
            java.util.Map<String, Object> result = adminService.verifyQRCode(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "INVALID",
                    "message", "QR Code not recognized"
            ));
        }
    }
    }
