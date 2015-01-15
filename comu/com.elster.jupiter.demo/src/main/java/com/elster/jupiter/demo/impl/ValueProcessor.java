package com.elster.jupiter.demo.impl;

import java.time.Instant;
import java.util.Collection;

public interface ValueProcessor {
    void processReadingTypes(Collection<String> readingTypes);
    void process(String readingType, Instant time, Double value, Object... additional);
    void stopProcessing();
}
