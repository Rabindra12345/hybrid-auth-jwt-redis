package com.raija.auth.services;

import com.raija.auth.dtos.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ** Need to pull from env **
    private static final long TTL = 7 * 24 * 60 * 60;

    @Autowired
    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void createSession(String sessionId, SessionData data) {
        redisTemplate.opsForValue().set(
                key(sessionId),
                data,
                Duration.ofSeconds(TTL)
        );
    }

    public SessionData getSession(String sessionId) {
        return (SessionData) redisTemplate.opsForValue().get(key(sessionId));
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }

    private String key(String sessionId) {
        return "session:" + sessionId;
    }
}
