package com.ananya.apireplay.dto;

import com.ananya.apireplay.diff.DiffEntry;

import java.util.List;

public record CallDiff(Long recordedCallId, String method, String path, boolean breaking, List<DiffEntry> diffs) {
}