package com.example.Ecomerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed-window per-IP rate limiter for the public endpoints called out by the challenge spec
 * (login, register, catalog browsing). In-memory only — fine for a single instance; a
 * multi-node deployment would need a shared store (e.g. Redis) instead.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    private final int authCapacity;
    private final int authWindowSeconds;
    private final int catalogCapacity;
    private final int catalogWindowSeconds;

    public RateLimitingFilter(
            @Value("${app.rate-limit.auth.capacity}") int authCapacity,
            @Value("${app.rate-limit.auth.window-seconds}") int authWindowSeconds,
            @Value("${app.rate-limit.catalog.capacity}") int catalogCapacity,
            @Value("${app.rate-limit.catalog.window-seconds}") int catalogWindowSeconds
    ) {
        this.authCapacity = authCapacity;
        this.authWindowSeconds = authWindowSeconds;
        this.catalogCapacity = catalogCapacity;
        this.catalogWindowSeconds = catalogWindowSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RateLimitRule rule = resolveRule(request);

        if (rule != null) {
            String key = rule.name() + ":" + clientIp(request);
            WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());

            if (!counter.tryConsume(rule.capacity(), rule.windowSeconds())) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                        "status", 429,
                        "error", "Too Many Requests",
                        "message", "Limite de requisições excedido. Tente novamente em instantes."
                )));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method) && (path.equals("/api/auth/login") || path.equals("/api/auth/register"))) {
            return new RateLimitRule("auth", authCapacity, authWindowSeconds);
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/products")) {
            return new RateLimitRule("catalog", catalogCapacity, catalogWindowSeconds);
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitRule(String name, int capacity, int windowSeconds) {
    }

    private static final class WindowCounter {
        private long windowStartMillis = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean tryConsume(int capacity, int windowSeconds) {
            long now = System.currentTimeMillis();
            long windowMillis = windowSeconds * 1000L;
            if (now - windowStartMillis >= windowMillis) {
                windowStartMillis = now;
                count = 0;
            }
            if (count >= capacity) {
                return false;
            }
            count++;
            return true;
        }
    }
}
