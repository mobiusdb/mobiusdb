package com.github.tanbamboo.mobiusdb.melt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeTest {
    @Test
    void overlapsWhenRangesIntersect() {
        TimeRange range = new TimeRange(10, 20);

        assertTrue(range.overlaps(5, 10));
        assertTrue(range.overlaps(15, 25));
        assertTrue(range.overlaps(12, 18));
    }

    @Test
    void doesNotOverlapWhenDisjoint() {
        TimeRange range = new TimeRange(10, 20);

        assertFalse(range.overlaps(1, 9));
        assertFalse(range.overlaps(21, 30));
    }
}
