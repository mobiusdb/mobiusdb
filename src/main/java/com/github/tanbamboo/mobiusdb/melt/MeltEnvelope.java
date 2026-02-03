package com.github.tanbamboo.mobiusdb.melt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MeltEnvelope {
    private final String id;
    private final SignalType signalType;
    private final long timestamp;
    private final Map<String, String> tags;
    private final String payload;

    public MeltEnvelope(String id, SignalType signalType, long timestamp, Map<String, String> tags, String payload) {
        this.id = Objects.requireNonNull(id, "id");
        this.signalType = Objects.requireNonNull(signalType, "signalType");
        this.timestamp = timestamp;
        this.tags = Collections.unmodifiableMap(Objects.requireNonNull(tags, "tags"));
        this.payload = Objects.requireNonNull(payload, "payload");
    }

    public static MeltEnvelope create(SignalType signalType, long timestamp, Map<String, String> tags, String payload) {
        return new MeltEnvelope(UUID.randomUUID().toString(), signalType, timestamp, tags, payload);
    }

    public String getId() {
        return id;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getPayload() {
        return payload;
    }
}
