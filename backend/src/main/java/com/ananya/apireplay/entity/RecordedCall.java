package com.ananya.apireplay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "recorded_calls")
public class RecordedCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String path;
    private String queryString;
    private int status;
    private long latencyMs;
    private Instant recordedAt;

    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    public RecordedCall() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getQueryString() { return queryString; }
    public void setQueryString(String queryString) { this.queryString = queryString; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }

    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}