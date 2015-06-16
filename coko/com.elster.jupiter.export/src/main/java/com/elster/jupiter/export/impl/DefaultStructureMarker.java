package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.util.UpdatableHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class DefaultStructureMarker implements StructureMarker {

    private final DefaultStructureMarker parent;
    private final String structure;
    private final List<String> path;
    private final Range<Instant> period;
    private final Clock clock;

    private DefaultStructureMarker(Clock clock, DefaultStructureMarker parent, String structure, Range<Instant> period) {
        this.parent = parent;
        this.structure = structure;
        this.clock = clock;
        this.path = parent == null ? Collections.singletonList(structure) : ImmutableList.<String>builder().addAll(parent.path).add(structure).build();
        this.period = period;
    }

    public static DefaultStructureMarker createRoot(Clock clock, String root) {
        return new DefaultStructureMarker(clock, null, root, null);
    }

    @Override
    public StructureMarker withPeriod(Range<Instant> period) {
        return new DefaultStructureMarker(clock, parent, structure, period);
    }

    @Override
    public Optional<Range<Instant>> getPeriod() {
        return Optional.ofNullable(period);
    }

    @Override
    public List<String> getStructurePath() {
        return path;
    }

    @Override
    public Optional<StructureMarker> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public DefaultStructureMarker child(String structure) {
        return new DefaultStructureMarker(clock, this, structure, period);
    }

    public DefaultStructureMarker adopt(StructureMarker structureMarker) {
        UpdatableHolder<DefaultStructureMarker> holder = new UpdatableHolder<>(this);
        structureMarker.getStructurePath().stream()
                .forEach(element -> holder.update(DefaultStructureMarker::child, element));
        return holder.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultStructureMarker that = (DefaultStructureMarker) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

}
