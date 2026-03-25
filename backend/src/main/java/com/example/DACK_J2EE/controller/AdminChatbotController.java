package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.service.AiAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-chatbot")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminChatbotController {

    @Autowired
    private AiAgentService aiAgentService;

    @PostMapping
    public ResponseEntity<?> askAdminQuestion(@RequestBody Map<String, String> payload) {
        try {
            String question = payload.get("question");
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("error", "Câu hỏi không được để trống"));
            }
            String reply = aiAgentService.askAdmin(question);
            return ResponseEntity.ok(Map.of("response", reply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Đã có lỗi xảy ra khi xử lý yêu cầu: " + e.getMessage()));
        }
    }
}
