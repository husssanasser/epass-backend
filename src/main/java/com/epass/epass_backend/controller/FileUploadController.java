package com.epass.epass_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://192.168.1.170:3001", "https://epass-frontend-wine.vercel.app"})
public class FileUploadController {

    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "djy3eqino",
            "api_key", "658765122737133",
            "api_secret", System.getenv().getOrDefault("CLOUDINARY_SECRET", "9qr5u-DgdUX09ATK4szlmkTz6o8")
    ));

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "epass-documents",
                    "resource_type", "auto"
            ));
            String url = (String) uploadResult.get("secure_url");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}