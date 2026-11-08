package com.ananya.apireplay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "replay_results")
public class ReplayResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String runId;
    private Long recordedCallId;
    private String method;
    private String path;
    private int expectedStatus;
    private int actualStatus;
    private long latencyMs;
    private Instant replayedAt;

    @Column(columnDefinition = "TEXT")
    private String expectedBody;

    @Column(columnDefinition = "TEXT")
    private String actualBody;

    public ReplayResult() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public Long getRecordedCallId() { return recordedCallId; }
    public void setRecordedCallId(Long recordedCallId) { this.recordedCallId = recordedCallId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public int getExpectedStatus() { return expectedStatus; }
    public void setExpectedStatus(int expectedStatus) { this.expectedStatus = expectedStatus; }

    public int getActualStatus() { return actualStatus; }
    public void setActualStatus(int actualStatus) { this.actualStatus = actualStatus; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public Instant getReplayedAt() { return replayedAt; }
    public void setReplayedAt(Instant replayedAt) { this.replayedAt = replayedAt; }

    public String getExpectedBody() { return expectedBody; }
    public void setExpectedBody(String expectedBody) { this.expectedBody = expectedBody; }

    public String getActualBody() { return actualBody; }
    public void setActualBody(String actualBody) { this.actualBody = actualBody; }
}