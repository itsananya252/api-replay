package com.ananya.apireplay.dto;

import java.time.Instant;

public record RunSummary(String runId, Instant replayedAt, int total) {
}