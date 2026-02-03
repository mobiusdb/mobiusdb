package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagCodecTest {
    @Test
    void encodeDecodeRoundTrip() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("service", "order-api");
        tags.put("env", "prod");
        tags.put("special", "a b&c=d");

        String encoded = TagCodec.encodeTags(tags);
        Map<String, String> decoded = TagCodec.decodeTags(encoded);

        assertEquals(tags, decoded);
    }

    @Test
    void decodeEmptyString() {
        Map<String, String> decoded = TagCodec.decodeTags("");

        assertTrue(decoded.isEmpty());
    }
}
