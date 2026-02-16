package com.douglasavila.cardservice.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class ApiUsageLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("API_USAGE");
    private static final String HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(HEADER))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString());

        long start = System.nanoTime();
        MDC.put("requestId", requestId);
        response.setHeader(HEADER, requestId);

        Exception exception = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            exception = ex;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.info(
                    "method={} path={} status={} contentType={} contentLength={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    response.getContentType(),
                    response.getHeader("Content-Length"),
                    durationMs
            );

            MDC.clear();
        }
    }
}
