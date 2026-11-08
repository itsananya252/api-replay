package com.ananya.apireplay.diff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class JsonDiffEngine {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final List<String> ignoredFields;

    public JsonDiffEngine(@Value("${replay.ignore-fields:}") List<String> ignoredFields) {
        this.ignoredFields = ignoredFields;
    }

    public List<DiffEntry> compare(String expectedBody, String actualBody) {
        List<DiffEntry> diffs = new ArrayList<>();

        JsonNode expected = parse(expectedBody);
        JsonNode actual = parse(actualBody);

        if (expected == null || actual == null) {
            if (!Objects.equals(expectedBody, actualBody)) {
                diffs.add(new DiffEntry("$", DiffType.VALUE_CHANGED, expectedBody, actualBody, false));
            }
            return diffs;
        }

        compareNodes("$", expected, actual, diffs);
        return diffs;
    }

    private JsonNode parse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return mapper.readTree(body);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isIgnored(String path) {
        int dot = path.lastIndexOf('.');
        String name = dot >= 0 ? path.substring(dot + 1) : path;
        int bracket = name.indexOf('[');
        if (bracket >= 0) {
            name = name.substring(0, bracket);
        }
        return ignoredFields.contains(name);
    }

    private void compareNodes(String path, JsonNode expected, JsonNode actual, List<DiffEntry> diffs) {
        if (expected.getNodeType() != actual.getNodeType()) {
            diffs.add(new DiffEntry(path, DiffType.TYPE_CHANGED, expected.toString(), actual.toString(), true));
            return;
        }

        if (expected.isObject()) {
            for (Map.Entry<String, JsonNode> field : expected.properties()) {
                String name = field.getKey();
                String childPath = path + "." + name;
                if (!actual.has(name)) {
                    diffs.add(new DiffEntry(childPath, DiffType.REMOVED, field.getValue().toString(), null, true));
                } else {
                    compareNodes(childPath, field.getValue(), actual.get(name), diffs);
                }
            }
            for (Map.Entry<String, JsonNode> field : actual.properties()) {
                if (!expected.has(field.getKey())) {
                    diffs.add(new DiffEntry(path + "." + field.getKey(), DiffType.ADDED,
                            null, field.getValue().toString(), false));
                }
            }
        } else if (expected.isArray()) {
            int max = Math.max(expected.size(), actual.size());
            for (int i = 0; i < max; i++) {
                String childPath = path + "[" + i + "]";
                if (i >= actual.size()) {
                    diffs.add(new DiffEntry(childPath, DiffType.REMOVED, expected.get(i).toString(), null, false));
                } else if (i >= expected.size()) {
                    diffs.add(new DiffEntry(childPath, DiffType.ADDED, null, actual.get(i).toString(), false));
                } else {
                    compareNodes(childPath, expected.get(i), actual.get(i), diffs);
                }
            }
        } else if (!expected.equals(actual)) {
            if (isIgnored(path)) {
                return;
            }
            diffs.add(new DiffEntry(path, DiffType.VALUE_CHANGED, expected.toString(), actual.toString(), false));
        }
    }
}