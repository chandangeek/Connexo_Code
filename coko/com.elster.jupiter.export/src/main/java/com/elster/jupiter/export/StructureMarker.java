package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StructureMarker {

    List<String> getStructurePath();

    Optional<StructureMarker> getParent();

    StructureMarker child(String structure);

    StructureMarker adopt(StructureMarker structureMarker);

    StructureMarker withPeriod(Range<Instant> period);

    Optional<Range<Instant>> getPeriod();
}
