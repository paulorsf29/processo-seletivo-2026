package com.example.Ecomerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Logs one JSON line per request: timestamp, method, route, status code and duration. */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("timestamp", Instant.now().toString());
            entry.put("method", request.getMethod());
            entry.put("path", request.getRequestURI());
            entry.put("status", response.getStatus());
            entry.put("durationMs", durationMs);

            try {
                log.info(objectMapper.writeValueAsString(entry));
            } catch (Exception ignored) {
                // Logging must never break the request.
            }
        }
    }
}
