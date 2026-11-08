package com.ananya.apireplay.dto;

import java.util.List;

public record ReplayReport(String runId, int total, int unchanged, int changed, int breaking, List<CallDiff> calls) {
}