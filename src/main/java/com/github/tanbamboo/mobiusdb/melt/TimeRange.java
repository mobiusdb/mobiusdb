package com.github.tanbamboo.mobiusdb.melt;

public final class TimeRange {
    private final long startInclusive;
    private final long endInclusive;

    public TimeRange(long startInclusive, long endInclusive) {
        if (endInclusive < startInclusive) {
            throw new IllegalArgumentException("end must be >= start");
        }
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    public long getStartInclusive() {
        return startInclusive;
    }

    public long getEndInclusive() {
        return endInclusive;
    }

    public boolean overlaps(long start, long end) {
        return !(end < startInclusive || start > endInclusive);
    }
}
