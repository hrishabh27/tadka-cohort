package com.tadka.api.infrastructure.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {

    private record Window(long minuteWindow, AtomicInteger count) {}

    private final Map<String, Window> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String clientIp = httpRequest.getRemoteAddr();
            long currentMinute = Instant.now().getEpochSecond() / 60;

            Window window = requestCounts.compute(clientIp, (ip, current) -> {
                if (current == null || current.minuteWindow() != currentMinute) {
                    return new Window(currentMinute, new AtomicInteger(1));
                }
                current.count().incrementAndGet();
                return current;
            });

            if (window.count().get() > MAX_REQUESTS_PER_MINUTE) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/problem+json");
                httpResponse.getWriter().write("""
                    {
                        "type": "https://tadka.com/errors/rate-limit-exceeded",
                        "title": "Too Many Requests",
                        "status": 429,
                        "detail": "Rate limit exceeded. Maximum 100 requests per minute."
                    }
                    """);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
