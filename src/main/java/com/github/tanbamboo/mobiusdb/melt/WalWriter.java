package com.github.tanbamboo.mobiusdb.melt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class WalWriter {
    private final Path walPath;

    public WalWriter(Path rootDirectory) throws IOException {
        this.walPath = rootDirectory.resolve("wal.log");
        if (!Files.exists(walPath)) {
            Files.createFile(walPath);
        }
    }

    public void append(MeltEnvelope envelope) throws IOException {
        String encoded = encodeEnvelope(envelope);
        Files.writeString(
            walPath,
            encoded + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.APPEND
        );
    }

    public List<MeltEnvelope> recover() throws IOException {
        if (!Files.exists(walPath)) {
            return List.of();
        }
        List<MeltEnvelope> envelopes = new ArrayList<>();
        for (String line : Files.readAllLines(walPath, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            envelopes.add(decodeEnvelope(line));
        }
        return envelopes;
    }

    public void clear() throws IOException {
        Files.writeString(walPath, "", StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
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
