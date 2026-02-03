package com.github.tanbamboo.mobiusdb.melt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MeltStore {
    private static final int DEFAULT_MAX_SEGMENT_RECORDS = 1000;

    private final Path segmentsDirectory;
    private final int maxSegmentRecords;
    private final List<SegmentWriter> segments;

    public MeltStore(Path rootDirectory) throws IOException {
        this(rootDirectory, DEFAULT_MAX_SEGMENT_RECORDS);
    }

    public MeltStore(Path rootDirectory, int maxSegmentRecords) throws IOException {
        this.segmentsDirectory = Objects.requireNonNull(rootDirectory, "rootDirectory")
            .resolve("segments");
        this.maxSegmentRecords = maxSegmentRecords;
        this.segments = new ArrayList<>();
        Files.createDirectories(segmentsDirectory);
        loadExistingSegments();
        if (segments.isEmpty()) {
            segments.add(SegmentWriter.createNew(segmentsDirectory, nextSegmentId()));
        }
    }

    public void append(MeltEnvelope envelope) throws IOException {
        SegmentWriter active = activeSegment();
        if (active.getMetadata().getCount() >= maxSegmentRecords) {
            active = SegmentWriter.createNew(segmentsDirectory, nextSegmentId());
            segments.add(active);
        }
        active.append(envelope);
    }

    public void appendRecovered(List<MeltEnvelope> envelopes) throws IOException {
        for (MeltEnvelope envelope : envelopes) {
            append(envelope);
        }
    }

    public List<MeltEnvelope> query(TimeRange range, TagFilter filter) throws IOException {
        List<MeltEnvelope> results = new ArrayList<>();
        for (SegmentWriter segment : segments) {
            SegmentMetadata metadata = segment.getMetadata();
            if (range != null && !metadata.overlaps(range)) {
                continue;
            }
            if (filter != null && !metadata.matches(filter)) {
                continue;
            }
            results.addAll(segment.query(range, filter));
        }
        return results;
    }

    private SegmentWriter activeSegment() {
        return segments.get(segments.size() - 1);
    }

    private void loadExistingSegments() throws IOException {
        List<Path> metadataFiles = Files.list(segmentsDirectory)
            .filter(path -> path.getFileName().toString().endsWith(".meta"))
            .sorted(Comparator.comparing(path -> path.getFileName().toString()))
            .collect(Collectors.toList());
        for (Path metadataPath : metadataFiles) {
            String baseName = metadataPath.getFileName().toString().replace(".meta", "");
            Path dataPath = segmentsDirectory.resolve(baseName + ".log");
            segments.add(SegmentWriter.loadExisting(dataPath, metadataPath));
        }
    }

    private String nextSegmentId() {
        return "segment-" + System.currentTimeMillis();
    }
}
