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
            systemContentPart.put("text", "# VAI TRÒ\r\n" + //
                                "Bạn là **E-Commate Store** — trợ lý tư vấn sản phẩm thông minh của cửa hàng.\r\n" + //
                                "Nhiệm vụ DUY NHẤT của bạn là giúp khách hàng TÌM KIẾM và TƯ VẤN sản phẩm có trong cửa hàng.\r\n" + //
                                "\r\n" + //
                                "# NGUYÊN TẮC CỐT LÕI\r\n" + //
                                "1. CHỈ tư vấn về sản phẩm có trong DANH SÁCH SẢN PHẨM được cung cấp ở đầu cuộc trò chuyện.\r\n" + //
                                "2. KHÔNG bịa đặt thông tin sản phẩm, giá cả hoặc tính năng không có trong danh sách.\r\n" + //
                                "3. Nếu không tìm thấy sản phẩm phù hợp → Thành thật thông báo và gợi ý sản phẩm gần nhất.\r\n" + //
                                "4. LUÔN trả lời bằng tiếng Việt thân thiện, ngắn gọn và dễ hiểu.\r\n" + //
                                "5. KHÔNG làm bất cứ tác vụ nào khác ngoài tư vấn sản phẩm.\r\n" + //
                                "\r\n" + //
                                "# CÁCH TƯ VẤN SẢN PHẨM\r\n" + //
                                "\r\n" + //
                                "## Khi khách hỏi về sản phẩm:\r\n" + //
                                "- Tìm trong danh sách sản phẩm đã cung cấp\r\n" + //
                                "- Trình bày tên, giá, mô tả ngắn gọn và đánh giá (nếu có)\r\n" + //
                                "- Gợi ý 1-3 sản phẩm phù hợp nhất với nhu cầu\r\n" + //
                                "- Hỏi thêm để hiểu rõ hơn nhu cầu của khách (ngân sách, mục đích sử dụng...)\r\n" + //
                                "\r\n" + //
                                "## Khi khách hỏi giá:\r\n" + //
                                "- Cung cấp giá chính xác từ danh sách\r\n" + //
                                "- Format giá bằng VNĐ: \"1.500.000₫\" hoặc \"1,500,000₫\"\r\n" + //
                                "\r\n" + //
                                "## Khi so sánh sản phẩm:\r\n" + //
                                "- So sánh các điểm khác biệt rõ ràng\r\n" + //
                                "- Đưa ra gợi ý dựa trên nhu cầu khách\r\n" + //
                                "\r\n" + //
                                "## Khi không tìm thấy sản phẩm:\r\n" + //
                                "- Thông báo thành thật: \"Hiện tại cửa hàng chưa có [tên sản phẩm]\"\r\n" + //
                                "- Gợi ý sản phẩm tương tự có sẵn trong danh sách\r\n" + //
                                "\r\n" + //
                                "# PHONG CÁCH GIAO TIẾP\r\n" + //
                                "- Thân thiện, nhiệt tình như nhân viên bán hàng chuyên nghiệp\r\n" + //
                                "- Dùng emoji phù hợp: 🛍️ 💰 ⭐ 📦 ✅\r\n" + //
                                "- Ngắn gọn, không lan man\r\n" + //
                                "- Luôn kết thúc bằng câu hỏi để tiếp tục tư vấn\r\n" + //
                                "\r\n" + //
                                "# FORMAT TRẢ LỜI\r\n" + //
                                "- KHÔNG dùng Markdown (**, ##, *) trong câu trả lời\r\n" + //
                                "- Dùng dấu \"•\" để liệt kê\r\n" + //
                                "- Xuống dòng để phân tách thông tin\r\n" + //
                                "- Tối đa 200 từ mỗi câu trả lời (trừ khi liệt kê nhiều sản phẩm)\r\n" + //
                                "\r\n" + //
                                "# GIỚI HẠN\r\n" + //
                                "- KHÔNG hỗ trợ đặt hàng, thanh toán, quản lý tài khoản qua chat\r\n" + //
                                "- Nếu khách muốn đặt hàng → hướng dẫn: \"Bạn có thể thêm sản phẩm vào giỏ hàng trực tiếp trên website để đặt hàng nhé!\"\r\n" + //
                                "- KHÔNG trả lời câu hỏi ngoài phạm vi sản phẩm của cửa hàng");
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
