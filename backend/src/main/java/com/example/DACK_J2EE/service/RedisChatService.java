package com.example.DACK_J2EE.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisChatService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long TTL_SESSION = 7 * 24 * 60 * 60; // 7 days
    private static final long TTL_MESSAGES = 7 * 24 * 60 * 60; // 7 days
    private static final long TTL_ACTIVE_SESSIONS = 30 * 24 * 60 * 60; // 30 days

    private final ObjectMapper mapper = new ObjectMapper();

    public String getOrCreateSession(String userId, String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = userId + "_" + System.currentTimeMillis();
        }

        String sessionKey = "chat:session:" + userId + ":" + sessionId;
        Boolean exists = redisTemplate.hasKey(sessionKey);

        if (Boolean.FALSE.equals(exists)) {
            Map<String, String> sessionData = new HashMap<>();
            sessionData.put("sessionId", sessionId);
            sessionData.put("userId", userId);
            sessionData.put("createdAt", Instant.now().toString());
            sessionData.put("lastActivity", Instant.now().toString());
            sessionData.put("messageCount", "0");

            redisTemplate.opsForHash().putAll(sessionKey, sessionData);
            redisTemplate.expire(sessionKey, TTL_SESSION, TimeUnit.SECONDS);

            String activeKey = "chat:active:" + userId;
            redisTemplate.opsForSet().add(activeKey, sessionId);
            redisTemplate.expire(activeKey, TTL_ACTIVE_SESSIONS, TimeUnit.SECONDS);

        } else {
            redisTemplate.opsForHash().put(sessionKey, "lastActivity", Instant.now().toString());
            redisTemplate.expire(sessionKey, TTL_SESSION, TimeUnit.SECONDS);
        }

        return sessionId;
    }

    public void addMessage(String userId, String sessionId, String role, String content) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", role);
            message.put("content", content);
            message.put("timestamp", Instant.now().toString());

            String messageJson = mapper.writeValueAsString(message);
            String messagesKey = "chat:messages:" + userId + ":" + sessionId;
            String sessionKey = "chat:session:" + userId + ":" + sessionId;

            redisTemplate.opsForList().rightPush(messagesKey, messageJson);
            redisTemplate.expire(messagesKey, TTL_MESSAGES, TimeUnit.SECONDS);

            Long messageCount = redisTemplate.opsForList().size(messagesKey);
            
            redisTemplate.opsForHash().put(sessionKey, "lastActivity", Instant.now().toString());
            if (messageCount != null) {
                redisTemplate.opsForHash().put(sessionKey, "messageCount", messageCount.toString());
            }
            redisTemplate.expire(sessionKey, TTL_SESSION, TimeUnit.SECONDS);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getMessages(String userId, String sessionId, int limit, int offset) {
        String messagesKey = "chat:messages:" + userId + ":" + sessionId;

        Boolean exists = redisTemplate.hasKey(messagesKey);
        if (Boolean.FALSE.equals(exists)) {
            return new ArrayList<>();
        }

        long start = -limit - offset;
        long end = offset == 0 ? -1 : -offset - 1;

        List<String> rawMessages = redisTemplate.opsForList().range(messagesKey, start, end);
        if (rawMessages == null) return new ArrayList<>();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String msg : rawMessages) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = mapper.readValue(msg, Map.class);
                resultList.add(parsed);
            } catch (JsonProcessingException e) {
                resultList.add(new HashMap<String, Object>());
            }
        }
        return resultList;
    }

    public List<Map<Object, Object>> getActiveSessions(String userId) {
        String activeKey = "chat:active:" + userId;
        Set<String> sessionIds = redisTemplate.opsForSet().members(activeKey);

        if (sessionIds == null || sessionIds.isEmpty()) return new ArrayList<>();

        List<Map<Object, Object>> sessions = new ArrayList<>();
        for (String sId : sessionIds) {
            String sessionKey = "chat:session:" + userId + ":" + sId;
            Map<Object, Object> data = redisTemplate.opsForHash().entries(sessionKey);

            if (data.isEmpty()) {
                redisTemplate.opsForSet().remove(activeKey, sId);
            } else {
                sessions.add(data);
            }
        }

        sessions.sort((a, b) -> {
            Instant timeA = Instant.parse(a.get("lastActivity").toString());
            Instant timeB = Instant.parse(b.get("lastActivity").toString());
            return timeB.compareTo(timeA);
        });

        return sessions;
    }

    public void deleteSession(String userId, String sessionId) {
        String sessionKey = "chat:session:" + userId + ":" + sessionId;
        String messagesKey = "chat:messages:" + userId + ":" + sessionId;
        String activeKey = "chat:active:" + userId;

        redisTemplate.delete(sessionKey);
        redisTemplate.delete(messagesKey);
        redisTemplate.opsForSet().remove(activeKey, sessionId);
    }

    public void deleteAllSessions(String userId) {
        List<Map<Object, Object>> sessions = getActiveSessions(userId);
        for (Map<Object, Object> s : sessions) {
            deleteSession(userId, s.get("sessionId").toString());
        }
    }
}
