package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeltStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void appendsAcrossSegmentsAndQueries() throws IOException {
        MeltStore store = new MeltStore(tempDir, 1);
        MeltEnvelope first = new MeltEnvelope("id-1", SignalType.METRIC, 100L, Map.of("env", "prod"), "one");
        MeltEnvelope second = new MeltEnvelope("id-2", SignalType.METRIC, 200L, Map.of("env", "stage"), "two");

        store.append(first);
        store.append(second);

        List<MeltEnvelope> all = store.query(new TimeRange(50, 250), new TagFilter(Map.of()));
        assertEquals(2, all.size());

        List<MeltEnvelope> filtered = store.query(new TimeRange(150, 250), new TagFilter(Map.of("env", "stage")));
        assertEquals(1, filtered.size());
        assertEquals("id-2", filtered.get(0).getId());
    }

    @Test
    void reloadsExistingSegments() throws IOException {
        MeltStore store = new MeltStore(tempDir, 1);
        store.append(new MeltEnvelope("id-1", SignalType.LOG, 100L, Map.of("service", "a"), "payload"));

        MeltStore reloaded = new MeltStore(tempDir, 1);
        List<MeltEnvelope> results = reloaded.query(new TimeRange(0, 200), new TagFilter(Map.of("service", "a")));

        assertEquals(1, results.size());
    }
}
