package com.ananya.apireplay.diff;

public record DiffEntry(String path, DiffType type, String expected, String actual, boolean breaking) {
}