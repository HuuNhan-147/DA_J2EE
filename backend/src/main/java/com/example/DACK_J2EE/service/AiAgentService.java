package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.entity.Product;
import com.example.DACK_J2EE.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiAgentService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Autowired
    private RedisChatService redisChatService;

    @Autowired
    private ProductRepository productRepository;

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
            // Lấy danh sách sản phẩm thực tế từ DB
            List<Product> products = productRepository.findAll();
            String productContext = products.stream()
                .map(p -> String.format("- %s: Giá %sđ, Mô tả: %s, Ảnh: %s (ID: %d)", 
                        p.getName(), p.getPrice().toString(), p.getDescription(), p.getImage(), p.getId()))
                .collect(Collectors.joining("\n"));

            String systemPrompt = "# VAI TRÒ\n" +
                                "Bạn là trợ lý bán hàng chuyên nghiệp của cửa hàng E-Commate.\n" +
                                "Nhiệm vụ của bạn là tư vấn cho khách hàng dựa trên DANH SÁCH SẢN PHẨM THỰC TẾ dưới đây.\n\n" +
                                "# DANH SÁCH SẢN PHẨM CỦA CỬA HÀNG:\n" +
                                productContext + "\n\n" +
                                "# NGUYÊN TẮC:\n" +
                                "1. CHỈ tư vấn những sản phẩm có trong danh sách trên.\n" +
                                "2. Nếu khách hỏi sản phẩm không có, hãy lịch sự thông báo và gợi ý sản phẩm tương tự đang có sẵn.\n" +
                                "3. Luôn thân thiện, dùng emoji 🛍️, ✅, ⭐.\n" +
                                "4. Trả lời ngắn gọn, tập trung vào tính năng và giá cả.\n" +
                                "5. ĐỊNH DẠNG ĐẶC BIỆT: Khi bạn muốn giới thiệu một hoặc nhiều sản phẩm cụ thể để khách hàng xem, hãy thêm mã này vào CUỐI câu trả lời:\n" +
                                "   [PRODUCTS][{\"id\": ID, \"name\": \"Tên SP\", \"price\": GIÁ_SỐ, \"image\": \"URL_ẢNH\"}][/PRODUCTS]\n" +
                                "   (Lấy chính xác thông tin từ danh sách được cung cấp. URL ảnh nếu là tương đối hãy giữ nguyên)\n" +
                                "6. Không trả lời các câu hỏi ngoài lề không liên quan đến mua sắm và sản phẩm của cửa hàng.";

            Map<String, Object> systemContentPart = new HashMap<>();
            systemContentPart.put("text", systemPrompt);
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
}
