package com.github.tanbamboo.mobiusdb.melt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;

final class SegmentMetadata {
    private final String segmentId;
    private long startTime;
    private long endTime;
    private int count;
    private final Map<String, Set<String>> tagIndex;

    SegmentMetadata(String segmentId) {
        this.segmentId = segmentId;
        this.startTime = Long.MAX_VALUE;
        this.endTime = Long.MIN_VALUE;
        this.count = 0;
        this.tagIndex = new HashMap<>();
    }

    String getSegmentId() {
        return segmentId;
    }

    long getStartTime() {
        return startTime;
    }

    long getEndTime() {
        return endTime;
    }

    int getCount() {
        return count;
    }

    Map<String, Set<String>> getTagIndex() {
        return Collections.unmodifiableMap(tagIndex);
    }

    void update(MeltEnvelope envelope) {
        startTime = Math.min(startTime, envelope.getTimestamp());
        endTime = Math.max(endTime, envelope.getTimestamp());
        count++;
        envelope.getTags().forEach((key, value) -> tagIndex
            .computeIfAbsent(key, ignored -> new HashSet<>())
            .add(value));
    }

    boolean overlaps(TimeRange range) {
        if (count == 0) {
            return false;
        }
        return range.overlaps(startTime, endTime);
    }

    boolean matches(TagFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> entry : filter.getRequired().entrySet()) {
            Set<String> values = tagIndex.get(entry.getKey());
            if (values == null || !values.contains(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    void save(Path metadataPath) throws IOException {
        Properties props = new Properties();
        props.setProperty("segmentId", segmentId);
        props.setProperty("startTime", Long.toString(startTime));
        props.setProperty("endTime", Long.toString(endTime));
        props.setProperty("count", Integer.toString(count));
        for (Map.Entry<String, Set<String>> entry : tagIndex.entrySet()) {
            StringJoiner joiner = new StringJoiner(",");
            for (String value : entry.getValue()) {
                joiner.add(value);
            }
            props.setProperty("tag." + entry.getKey(), joiner.toString());
        }
        try (OutputStream outputStream = Files.newOutputStream(metadataPath)) {
            props.store(outputStream, "segment metadata");
        }
    }

    static SegmentMetadata load(Path metadataPath) throws IOException {
        Properties props = new Properties();
        try (InputStream inputStream = Files.newInputStream(metadataPath)) {
            props.load(inputStream);
        }
        String id = props.getProperty("segmentId");
        SegmentMetadata metadata = new SegmentMetadata(id);
        metadata.startTime = Long.parseLong(props.getProperty("startTime", Long.toString(Long.MAX_VALUE)));
        metadata.endTime = Long.parseLong(props.getProperty("endTime", Long.toString(Long.MIN_VALUE)));
        metadata.count = Integer.parseInt(props.getProperty("count", "0"));
        for (String name : props.stringPropertyNames()) {
            if (name.startsWith("tag.")) {
                String key = name.substring("tag.".length());
                Set<String> values = new HashSet<>();
                String raw = props.getProperty(name, "");
                if (!raw.isBlank()) {
                    String[] parts = raw.split(",");
                    Collections.addAll(values, parts);
                }
                metadata.tagIndex.put(key, values);
            }
        }
        return metadata;
    }
}
