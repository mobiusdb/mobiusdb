package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MeltEnvelopeTest {
    @Test
    void createAssignsIdAndStoresFields() {
        MeltEnvelope envelope = MeltEnvelope.create(
            SignalType.LOG,
            1234L,
            Map.of("service", "api"),
            "payload"
        );

        assertNotNull(envelope.getId());
        assertEquals(SignalType.LOG, envelope.getSignalType());
        assertEquals(1234L, envelope.getTimestamp());
        assertEquals(Map.of("service", "api"), envelope.getTags());
        assertEquals("payload", envelope.getPayload());
    }
}
