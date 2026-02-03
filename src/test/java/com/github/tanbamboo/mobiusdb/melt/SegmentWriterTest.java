package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SegmentWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void appendAndQuerySegment() throws IOException {
        SegmentWriter writer = SegmentWriter.createNew(tempDir, "segment-1");
        MeltEnvelope envelope = new MeltEnvelope(
            "id-1",
            SignalType.LOG,
            200L,
            Map.of("service", "api"),
            "payload"
        );

        assertTrue(writer.append(envelope));

        List<MeltEnvelope> results = writer.query(new TimeRange(150, 250), new TagFilter(Map.of("service", "api")));

        assertEquals(1, results.size());
        assertEquals("id-1", results.get(0).getId());
    }

    @Test
    void doesNotAppendDuplicateIds() throws IOException {
        SegmentWriter writer = SegmentWriter.createNew(tempDir, "segment-1");
        MeltEnvelope envelope = new MeltEnvelope(
            "id-1",
            SignalType.LOG,
            200L,
            Map.of("service", "api"),
            "payload"
        );

        assertTrue(writer.append(envelope));
        assertFalse(writer.append(envelope));

        assertEquals(1, writer.getMetadata().getCount());
    }

    @Test
    void loadExistingSegmentPreservesIds() throws IOException {
        SegmentWriter writer = SegmentWriter.createNew(tempDir, "segment-1");
        MeltEnvelope envelope = new MeltEnvelope(
            "id-1",
            SignalType.LOG,
            200L,
            Map.of("service", "api"),
            "payload"
        );
        writer.append(envelope);

        SegmentWriter reloaded = SegmentWriter.loadExisting(
            tempDir.resolve("segment-1.log"),
            tempDir.resolve("segment-1.meta")
        );

        assertFalse(reloaded.append(envelope));
    }
}
