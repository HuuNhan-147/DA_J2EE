package com.example.DACK_J2EE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAgentService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Autowired
    private RedisChatService redisChatService;

    public Map<String, Object> processMessage(String message, String sessionId, String userId) {
        try {
            if (userId == null) {
                userId = "guest";
            }
            
            sessionId = redisChatService.getOrCreateSession(userId, sessionId);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = geminiApiUrl + "?key=" + geminiApiKey;

            // Load context history
            List<Map<String, Object>> messagesHistory = redisChatService.getMessages(userId, sessionId, 20, 0);
            
            List<Map<String, Object>> contents = new ArrayList<>();
            // Add system instruction as initial context
            Map<String, Object> systemContentPart = new HashMap<>();
            systemContentPart.put("text", "Bạn là trợ lý AI ảo cho website bán hàng DACK_J2EE tên là Antigravity. Hãy trả lời ngắn gọn và thân thiện.");
            Map<String, Object> systemPartMap = new HashMap<>();
            systemPartMap.put("role", "user");
            systemPartMap.put("parts", List.of(systemContentPart));
            contents.add(systemPartMap);
            
            // Add history
            for (Map<String, Object> msg : messagesHistory) {
                Map<String, Object> contentPart = new HashMap<>();
                contentPart.put("text", msg.get("content"));
                
                Map<String, Object> partMap = new HashMap<>();
                partMap.put("role", msg.get("role"));
                partMap.put("parts", List.of(contentPart));
                contents.add(partMap);
            }

            // Add current message
            Map<String, Object> currentContentPart = new HashMap<>();
            currentContentPart.put("text", message);
            Map<String, Object> currentPartMap = new HashMap<>();
            currentPartMap.put("role", "user");
            currentPartMap.put("parts", List.of(currentContentPart));
            contents.add(currentPartMap);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        String replyText = (String) parts.get(0).get("text");
                        
                        // Save both user message and AI response
                        redisChatService.addMessage(userId, sessionId, "user", message);
                        redisChatService.addMessage(userId, sessionId, "model", replyText);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", true);
                        result.put("reply", replyText);
                        result.put("sessionId", sessionId);
                        result.put("iteration", 1);
                        result.put("payload", null);
                        return result;
                    }
                }
            }

            return Map.of("success", false, "reply", "Không có phản hồi từ AI.");

        } catch (Exception e) {
             e.printStackTrace();
             return Map.of("success", false, "reply", "Xin lỗi, đã xảy ra lỗi trong quá trình xử lý AI: " + e.getMessage());
        }
    }

    public String askAdmin(String question) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = geminiApiUrl + "?key=" + geminiApiKey;

            Map<String, Object> contentPart = new HashMap<>();
            contentPart.put("text", "Bạn là AI hỗ trợ quản trị viên hệ thống. Trả lời câu hỏi này về quản trị hệ thống: " + question);

            Map<String, Object> partMap = new HashMap<>();
            partMap.put("parts", List.of(contentPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(partMap));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            return "Không có phản hồi.";
        } catch(Exception e) {
            e.printStackTrace();
            return "Lỗi server khi hỏi AI: " + e.getMessage();
        }
    }
}
