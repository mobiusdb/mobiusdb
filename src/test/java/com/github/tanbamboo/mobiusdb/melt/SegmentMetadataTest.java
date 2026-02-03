package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SegmentMetadataTest {
    @TempDir
    Path tempDir;

    @Test
    void updatesAndPersistsMetadata() throws IOException {
        SegmentMetadata metadata = new SegmentMetadata("segment-1");
        MeltEnvelope envelope = new MeltEnvelope(
            "id-1",
            SignalType.METRIC,
            100L,
            Map.of("host", "a"),
            "payload"
        );

        metadata.update(envelope);

        assertEquals(100L, metadata.getStartTime());
        assertEquals(100L, metadata.getEndTime());
        assertEquals(1, metadata.getCount());
        assertTrue(metadata.matches(new TagFilter(Map.of("host", "a"))));
        assertFalse(metadata.matches(new TagFilter(Map.of("host", "b"))));
        assertTrue(metadata.overlaps(new TimeRange(50, 150)));

        Path metadataPath = tempDir.resolve("segment-1.meta");
        metadata.save(metadataPath);
        SegmentMetadata loaded = SegmentMetadata.load(metadataPath);

        assertEquals(metadata.getSegmentId(), loaded.getSegmentId());
        assertEquals(metadata.getStartTime(), loaded.getStartTime());
        assertEquals(metadata.getEndTime(), loaded.getEndTime());
        assertEquals(metadata.getCount(), loaded.getCount());
        assertEquals(metadata.getTagIndex(), loaded.getTagIndex());
    }
}
