package com.elster.jupiter.export;

import com.elster.jupiter.util.collections.DualIterable;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public interface StructureMarker {

    List<String> getStructurePath();

    Optional<StructureMarker> getParent();

    StructureMarker child(String structure);

    StructureMarker adopt(StructureMarker structureMarker);

    StructureMarker withPeriod(Range<Instant> period);

    Optional<Range<Instant>> getPeriod();

    default int differsAt(StructureMarker other) {
        return (int) decorate(DualIterable.endWithLongest(getStructurePath(), other.getStructurePath()).stream())
                .takeWhile(pair -> Objects.equals(pair.getFirst(), pair.getLast()))
                .count();
    }
}
