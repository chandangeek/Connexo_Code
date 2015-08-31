package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;

public interface DefaultSelectorOccurrence {

    Range<Instant> getExportedDataInterval();

}
