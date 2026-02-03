package com.github.tanbamboo.mobiusdb.melt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SegmentWriter {
    private final Path dataPath;
    private final Path metadataPath;
    private final SegmentMetadata metadata;
    private final Set<String> seenIds;

    SegmentWriter(Path dataPath, Path metadataPath, SegmentMetadata metadata, Set<String> seenIds) {
        this.dataPath = dataPath;
        this.metadataPath = metadataPath;
        this.metadata = metadata;
        this.seenIds = seenIds;
    }

    static SegmentWriter createNew(Path directory, String segmentId) throws IOException {
        Path dataPath = directory.resolve(segmentId + ".log");
        Path metadataPath = directory.resolve(segmentId + ".meta");
        SegmentMetadata metadata = new SegmentMetadata(segmentId);
        Files.createFile(dataPath);
        metadata.save(metadataPath);
        return new SegmentWriter(dataPath, metadataPath, metadata, new HashSet<>());
    }

    static SegmentWriter loadExisting(Path dataPath, Path metadataPath) throws IOException {
        SegmentMetadata metadata = SegmentMetadata.load(metadataPath);
        Set<String> ids = new HashSet<>();
        if (Files.exists(dataPath)) {
            for (MeltEnvelope envelope : readAll(dataPath)) {
                ids.add(envelope.getId());
            }
        }
        return new SegmentWriter(dataPath, metadataPath, metadata, ids);
    }

    SegmentMetadata getMetadata() {
        return metadata;
    }

    boolean append(MeltEnvelope envelope) throws IOException {
        if (seenIds.contains(envelope.getId())) {
            return false;
        }
        String encoded = encodeEnvelope(envelope);
        Files.writeString(
            dataPath,
            encoded + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.APPEND
        );
        metadata.update(envelope);
        metadata.save(metadataPath);
        seenIds.add(envelope.getId());
        return true;
    }

    List<MeltEnvelope> query(TimeRange range, TagFilter filter) throws IOException {
        List<MeltEnvelope> results = new ArrayList<>();
        for (MeltEnvelope envelope : readAll(dataPath)) {
            if (range != null && !range.overlaps(envelope.getTimestamp(), envelope.getTimestamp())) {
                continue;
            }
            if (filter != null && !filter.isEmpty()) {
                boolean matches = filter.getRequired().entrySet().stream()
                    .allMatch(entry -> entry.getValue().equals(envelope.getTags().get(entry.getKey())));
                if (!matches) {
                    continue;
                }
            }
            results.add(envelope);
        }
        return results;
    }

    private static List<MeltEnvelope> readAll(Path dataPath) throws IOException {
        if (!Files.exists(dataPath)) {
            return List.of();
        }
        List<MeltEnvelope> envelopes = new ArrayList<>();
        for (String line : Files.readAllLines(dataPath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            envelopes.add(decodeEnvelope(line));
        }
        return envelopes;
    }

    private static String encodeEnvelope(MeltEnvelope envelope) {
        String encodedTags = TagCodec.encodeTags(envelope.getTags());
        String payload = Base64.getEncoder()
            .encodeToString(envelope.getPayload().getBytes(StandardCharsets.UTF_8));
        return String.join(
            "|",
            envelope.getId(),
            Long.toString(envelope.getTimestamp()),
            envelope.getSignalType().name(),
            encodedTags,
            payload
        );
    }

    private static MeltEnvelope decodeEnvelope(String line) {
        String[] parts = line.split("\\|", 5);
        String id = parts[0];
        long timestamp = Long.parseLong(parts[1]);
        SignalType signalType = SignalType.valueOf(parts[2]);
        String encodedTags = parts.length > 3 ? parts[3] : "";
        String payload = "";
        if (parts.length > 4) {
            payload = new String(Base64.getDecoder().decode(parts[4]), StandardCharsets.UTF_8);
        }
        return new MeltEnvelope(id, signalType, timestamp, TagCodec.decodeTags(encodedTags), payload);
    }
}
