package com.epass.epass_backend.controller;

import com.epass.epass_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/verify")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://192.168.1.170:3001"})
public class VerifyController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/{token}")
    public ResponseEntity<?> verifyQRCode(@PathVariable String token) {
        try {
            Map<String, Object> result = adminService.verifyQRCode(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "INVALID",
                    "message", "QR Code not recognized"
            ));
        }
    }
}