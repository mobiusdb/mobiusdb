package com.github.tanbamboo.mobiusdb.melt;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class TagFilter {
    private final Map<String, String> required;

    public TagFilter(Map<String, String> required) {
        this.required = Collections.unmodifiableMap(Objects.requireNonNull(required, "required"));
    }

    public Map<String, String> getRequired() {
        return required;
    }

    public boolean isEmpty() {
        return required.isEmpty();
    }
}
