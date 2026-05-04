package com.raija.auth.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int AUTH_MAX_REQUESTS = 5;
    private static final int API_MAX_REQUESTS = 100;
    private static final long AUTH_WINDOW_SECONDS = 60;
    private static final long API_WINDOW_SECONDS = 60;

    private static final List<String> AUTH_PATHS =
            List.of("/auth/login", "/auth/signup", "/auth/refresh");

    public RateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String ip = req.getRemoteAddr();
        String uri = req.getRequestURI();
        boolean isAuthPath = AUTH_PATHS.stream().anyMatch(uri::contains);
        int maxRequests = isAuthPath ? AUTH_MAX_REQUESTS : API_MAX_REQUESTS;
        long windowSeconds = isAuthPath ? AUTH_WINDOW_SECONDS : API_WINDOW_SECONDS;
        // keeping separate keys for auth vs api
        String redisKey = isAuthPath
                ? "rate:auth:" + ip
                : "rate:api:" + ip;
        boolean allowed = isAllowed(redisKey, maxRequests, windowSeconds);
        long current = getCurrentCount(redisKey);
        res.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - current)));
        if (!allowed) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Too many requests. Try again later.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isAllowed(String key, int maxRequests, long windowSeconds) {
        // INCR is atomic in Redis so we face no race condition
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) return true;
        // first request,  set expiry
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count <= maxRequests;
    }

    private long getCurrentCount(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) return 0;
        return Long.parseLong(val.toString());
    }
}
