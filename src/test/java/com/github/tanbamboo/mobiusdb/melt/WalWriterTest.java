package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void appendRecoverAndClear() throws IOException {
        WalWriter walWriter = new WalWriter(tempDir);
        MeltEnvelope first = new MeltEnvelope("id-1", SignalType.EVENT, 1L, Map.of(), "one");
        MeltEnvelope second = new MeltEnvelope("id-2", SignalType.EVENT, 2L, Map.of(), "two");

        walWriter.append(first);
        walWriter.append(second);

        List<MeltEnvelope> recovered = walWriter.recover();
        assertEquals(2, recovered.size());

        walWriter.clear();

        assertEquals(0L, Files.size(tempDir.resolve("wal.log")));
    }
}
