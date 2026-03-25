package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.service.AiAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-agent")
@CrossOrigin(origins = "http://localhost:5173")
public class AiAgentController {

    @Autowired
    private AiAgentService aiAgentService;

    @PostMapping
    public ResponseEntity<?> runAgent(@RequestBody Map<String, String> payload) {
        try {
            String message = payload.get("message");
            String sessionId = payload.get("sessionId");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                userId = authentication.getName();
            }

            // Call service
            Map<String, Object> result = aiAgentService.processMessage(message, sessionId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "reply", "Error: " + e.getMessage()));
        }
    }
}
