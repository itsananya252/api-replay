package com.ananya.apireplay.diff;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonDiffEngineTest {

    private final JsonDiffEngine engine = new JsonDiffEngine(List.of("createdAt"));

    @Test
    void identicalJsonHasNoDiffs() {
        String json = "{\"id\":1,\"name\":\"Rahul\"}";

        assertThat(engine.compare(json, json)).isEmpty();
    }

    @Test
    void removedFieldIsBreaking() {
        List<DiffEntry> diffs = engine.compare("{\"id\":1,\"name\":\"Rahul\"}", "{\"id\":1}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).path()).isEqualTo("$.name");
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.REMOVED);
        assertThat(diffs.get(0).breaking()).isTrue();
    }

    @Test
    void addedFieldIsNotBreaking() {
        List<DiffEntry> diffs = engine.compare("{\"id\":1}", "{\"id\":1,\"email\":\"a@b.com\"}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).path()).isEqualTo("$.email");
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.ADDED);
        assertThat(diffs.get(0).breaking()).isFalse();
    }

    @Test
    void valueChangeIsNotBreaking() {
        List<DiffEntry> diffs = engine.compare("{\"name\":\"Rahul\"}", "{\"name\":\"Amit\"}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.VALUE_CHANGED);
        assertThat(diffs.get(0).breaking()).isFalse();
    }

    @Test
    void typeChangeIsBreaking() {
        List<DiffEntry> diffs = engine.compare("{\"id\":1}", "{\"id\":\"1\"}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.TYPE_CHANGED);
        assertThat(diffs.get(0).breaking()).isTrue();
    }

    @Test
    void ignoredFieldValueChangeIsSkipped() {
        List<DiffEntry> diffs = engine.compare(
                "{\"id\":1,\"createdAt\":\"2026-01-01\"}",
                "{\"id\":1,\"createdAt\":\"2026-02-02\"}");

        assertThat(diffs).isEmpty();
    }

    @Test
    void ignoredFieldRemovalIsStillBreaking() {
        List<DiffEntry> diffs = engine.compare("{\"id\":1,\"createdAt\":\"2026-01-01\"}", "{\"id\":1}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.REMOVED);
        assertThat(diffs.get(0).breaking()).isTrue();
    }

    @Test
    void nestedFieldPathIsReported() {
        List<DiffEntry> diffs = engine.compare("{\"user\":{\"name\":\"a\"}}", "{\"user\":{}}");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).path()).isEqualTo("$.user.name");
    }

    @Test
    void arrayElementAddedIsNotBreaking() {
        List<DiffEntry> diffs = engine.compare("[1]", "[1,2]");

        assertThat(diffs).hasSize(1);
        assertThat(diffs.get(0).path()).isEqualTo("$[1]");
        assertThat(diffs.get(0).type()).isEqualTo(DiffType.ADDED);
        assertThat(diffs.get(0).breaking()).isFalse();
    }
}