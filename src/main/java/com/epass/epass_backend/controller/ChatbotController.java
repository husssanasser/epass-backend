package com.epass.epass_backend.controller;

import com.epass.epass_backend.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String reply = chatbotService.chat(userMessage);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("reply", "Sorry, something went wrong.")
            );
        }
    }
}