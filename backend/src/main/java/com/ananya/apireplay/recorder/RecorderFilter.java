package com.ananya.apireplay.recorder;

import com.ananya.apireplay.entity.RecordedCall;
import com.ananya.apireplay.repository.RecordedCallRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Set;

@Component
public class RecorderFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RecorderFilter.class);
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "set-cookie");

    private final RecordedCallRepository recordedCallRepository;

    public RecorderFilter(RecordedCallRepository recordedCallRepository) {
        this.recordedCallRepository = recordedCallRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.startsWith("/api/recorded")
                || path.startsWith("/api/replay")
                || request.getHeader("X-Replay-Call") != null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long latency = System.currentTimeMillis() - start;

            try {
                RecordedCall call = new RecordedCall();
                call.setMethod(request.getMethod());
                call.setPath(request.getRequestURI());
                call.setQueryString(request.getQueryString());
                call.setStatus(wrappedResponse.getStatus());
                call.setLatencyMs(latency);
                call.setRecordedAt(Instant.now());
                call.setRequestHeaders(extractHeaders(request));
                call.setRequestBody(new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8));
                call.setResponseBody(new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8));

                recordedCallRepository.save(call);
                log.info("RECORDED {} {} | status={} | {}ms", call.getMethod(), call.getPath(), call.getStatus(), latency);
            } catch (Exception e) {
                log.warn("Recording failed: {}", e.getMessage());
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            String value = SENSITIVE_HEADERS.contains(name.toLowerCase()) ? "***" : request.getHeader(name);
            sb.append(name).append(": ").append(value).append("\n");
        }
        return sb.toString();
    }
}