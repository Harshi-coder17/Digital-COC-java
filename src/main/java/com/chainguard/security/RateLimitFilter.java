package com.chainguard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory fixed-window rate limiter to mitigate brute-force login attempts
 * and API DoS floods. Uses remoteAddr directly so spoofed X-Forwarded-For headers
 * cannot bypass limits.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;
    private static final int AUTH_LIMIT = 10;
    private static final int API_LIMIT = 120;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private record WindowCounter(long windowStart, AtomicInteger count) {}

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAuthEndpoint = path.equals("/api/auth/login");
        int limit = isAuthEndpoint ? AUTH_LIMIT : API_LIMIT;
        String key = (isAuthEndpoint ? "auth:" : "api:") + request.getRemoteAddr();
        long now = System.currentTimeMillis();

        if (counters.size() > MAX_TRACKED_KEYS) {
            counters.entrySet().removeIf(e -> now - e.getValue().windowStart() >= WINDOW_MS);
        }

        WindowCounter current = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_MS) {
                return new WindowCounter(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (current.count().get() > limit) {
            long retryAfterSec = Math.max(1, (WINDOW_MS - (now - current.windowStart())) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSec));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
