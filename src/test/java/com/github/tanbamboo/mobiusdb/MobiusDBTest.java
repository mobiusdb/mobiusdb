package com.github.tanbamboo.mobiusdb;

import com.github.tanbamboo.mobiusdb.melt.MeltEnvelope;
import com.github.tanbamboo.mobiusdb.melt.SignalType;
import com.github.tanbamboo.mobiusdb.melt.TagFilter;
import com.github.tanbamboo.mobiusdb.melt.TimeRange;
import com.github.tanbamboo.mobiusdb.melt.WalWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobiusDBTest {
    @TempDir
    Path tempDir;

    @Test
    void ingestsAndQueries() throws IOException {
        MobiusDB db = new MobiusDB(tempDir);
        MeltEnvelope envelope = MeltEnvelope.create(
            SignalType.LOG,
            100L,
            Map.of("service", "api"),
            "payload"
        );

        db.ingest(envelope);

        List<MeltEnvelope> results = db.query(new TimeRange(0, 200), new TagFilter(Map.of("service", "api")));

        assertEquals(1, results.size());
    }

    @Test
    void recoversFromWalOnStartup() throws IOException {
        WalWriter walWriter = new WalWriter(tempDir);
        MeltEnvelope envelope = new MeltEnvelope(
            "id-1",
            SignalType.EVENT,
            100L,
            Map.of("service", "api"),
            "payload"
        );
        walWriter.append(envelope);

        MobiusDB db = new MobiusDB(tempDir);

        List<MeltEnvelope> results = db.query(new TimeRange(0, 200), new TagFilter(Map.of("service", "api")));
        assertEquals(1, results.size());
        assertEquals(0L, Files.size(tempDir.resolve("wal.log")));
    }
}
