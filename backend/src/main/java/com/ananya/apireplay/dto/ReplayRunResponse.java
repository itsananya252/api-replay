package com.ananya.apireplay.dto;

public record ReplayRunResponse(String runId, String target, int total, int same, int changed) {
}