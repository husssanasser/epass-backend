package com.epass.epass_backend.service;

import com.epass.epass_backend.dto.LoginRequest;
import com.epass.epass_backend.dto.RegisterRequest;
import com.epass.epass_backend.model.AuditLog;
import com.epass.epass_backend.model.User;
import com.epass.epass_backend.repository.AuditLogRepository;
import com.epass.epass_backend.repository.UserRepository;
import com.epass.epass_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Register new user
    public Map<String, String> register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setRole(User.Role.USER);

        // Save user to database
        userRepository.save(user);

        // Save audit log
        AuditLog log = new AuditLog();
        log.setActorType("USER");
        log.setActorId(user.getId());
        log.setAction("REGISTER");
        log.setDetails("New user registered: " + user.getEmail());
        auditLogRepository.save(log);

        // Return success message
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return response;
    }

    // Login
    public Map<String, String> login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // Save audit log
        AuditLog log = new AuditLog();
        log.setActorType(user.getRole().name());
        log.setActorId(user.getId());
        log.setAction("LOGIN");
        log.setDetails("User logged in: " + user.getEmail());
        auditLogRepository.save(log);

        // Return token and user info
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("name", user.getFullName());
        response.put("email", user.getEmail());
        return response;
    }

    // Reset Password
    public Map<String, String> resetPassword(String email, String newPassword) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Save audit log
        AuditLog log = new AuditLog();
        log.setActorType("USER");
        log.setActorId(user.getId());
        log.setAction("RESET_PASSWORD");
        log.setDetails("Password reset for: " + email);
        auditLogRepository.save(log);

        // Return success message
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return response;
    }
}