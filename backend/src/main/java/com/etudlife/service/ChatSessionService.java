package com.etudlife.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final StringRedisTemplate redis;
    private final Duration ttl = Duration.ofMinutes(30); // ajuste si tu veux

    public ChatSessionService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String key(String sessionId) {
        return "chat:session:" + sessionId;
    }

    public String newSession() {
        String sessionId = UUID.randomUUID().toString();
        redis.opsForList().rightPush(key(sessionId), "");
        redis.opsForList().leftPop(key(sessionId));
        redis.expire(key(sessionId), ttl);
        return sessionId;
    }

    public void append(String sessionId, String role, String content) {
        String line = role + ":" + content;
        redis.opsForList().rightPush(key(sessionId), line);
        redis.expire(key(sessionId), ttl); // refresh TTL
    }

    public List<String> history(String sessionId, int lastN) {
        Long size = redis.opsForList().size(key(sessionId));
        if (size == null || size == 0) return List.of();
        long start = Math.max(0, size - lastN);
        return redis.opsForList().range(key(sessionId), start, -1);
    }

    public void delete(String sessionId) {
        redis.delete(key(sessionId));
    }
}
