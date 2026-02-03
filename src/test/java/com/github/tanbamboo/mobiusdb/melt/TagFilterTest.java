package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagFilterTest {
    @Test
    void storesRequiredTags() {
        TagFilter filter = new TagFilter(Map.of("service", "payments"));

        assertEquals(Map.of("service", "payments"), filter.getRequired());
        assertFalse(filter.isEmpty());
    }

    @Test
    void emptyFilterIsEmpty() {
        TagFilter filter = new TagFilter(Map.of());

        assertTrue(filter.isEmpty());
    }
}
