/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

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

final public class TestDefaultStructureMarker implements StructureMarker {

    private final TestDefaultStructureMarker parent;
    private final String structure;
    private final List<String> path;
    private final Range<Instant> period;
    private final Clock clock;

    private TestDefaultStructureMarker(Clock clock, TestDefaultStructureMarker parent, String structure, Range<Instant> period) {
        this.parent = parent;
        this.structure = structure;
        this.clock = clock;
        this.path = parent == null ? Collections.singletonList(structure) : ImmutableList.<String>builder().addAll(parent.path).add(structure).build();
        this.period = period;
    }

    public static TestDefaultStructureMarker createRoot(Clock clock, String root) {
        return new TestDefaultStructureMarker(clock, null, root, null);
    }

    @Override
    public StructureMarker withPeriod(Range<Instant> period) {
        return new TestDefaultStructureMarker(clock, parent, structure, period);
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
    public TestDefaultStructureMarker child(String structure) {
        return new TestDefaultStructureMarker(clock, this, structure, period);
    }

    public TestDefaultStructureMarker adopt(StructureMarker structureMarker) {
        UpdatableHolder<TestDefaultStructureMarker> holder = new UpdatableHolder<>(this);
        structureMarker.getStructurePath()
                .forEach(element -> holder.update(TestDefaultStructureMarker::child, element));
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
        TestDefaultStructureMarker that = (TestDefaultStructureMarker) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

}
