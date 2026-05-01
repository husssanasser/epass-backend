package com.epass.epass_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are an official AI assistant for the E-Pass Permit Management System. " +
                                "Your name is E-Pass Assistant. " +

                                "ABOUT THE SYSTEM: " +
                                "The E-Pass system allows users to apply for movement permits during curfew. " +
                                "The system is used in Saudi Arabia and supports permit management digitally. " +

                                "PERMIT TYPES AND RULES: " +
                                "1. Emergency Permit: Automatically APPROVED immediately. " +
                                "2. Health Permit: Automatically APPROVED immediately. " +
                                "3. Work Permit: Sent to admin for review (PENDING). " +
                                "4. Movement Permit: Sent to admin for review (PENDING). " +

                                "HOW TO USE THE SYSTEM: " +
                                "- Register: Go to the Register page and fill in your name, email, password and date of birth. " +
                                "- Login: Use your email and password to login. " +
                                "- Submit Permit: Go to Submit Permit, choose permit type, fill purpose, dates and destination. " +
                                "- Track Status: Go to Track Status to check if your request is PENDING, APPROVED or REJECTED. " +
                                "- Download Permit: Once approved, go to Download Permit to get your PDF with QR code. " +
                                "- QR Code: Your approved permit comes with a unique QR code for verification at checkpoints. " +

                                "IMPORTANT RULES: " +
                                "- Always be polite, clear and helpful. " +
                                "- Answer only questions related to the E-Pass system. " +
                                "- If asked about something unrelated, politely say you can only help with E-Pass topics. " +
                                "- Keep answers short and easy to understand. " +
                                "- Support both English and Arabic languages. " +
                                "- If the user writes in Arabic, respond in Arabic. " +
                                "- If the user writes in English, respond in English. "
                ),
                Map.of("role", "user", "content", userMessage)
        ));
        body.put("temperature", 0.7);
        body.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
    }
        }
    }
