package com.elster.jupiter.export;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultStructureMarker implements StructureMarker {

    private final DefaultStructureMarker parent;
    private final String structure;
    private final List<String> path;

    private DefaultStructureMarker(DefaultStructureMarker parent, String structure) {
        this.parent = parent;
        this.structure = structure;
        this.path = parent == null ? Collections.singletonList(structure) : ImmutableList.<String>builder().addAll(parent.path).add(structure).build();
    }

    public static DefaultStructureMarker createRoot(String root) {
        return new DefaultStructureMarker(null, root);
    }

    @Override
    public List<String> getStructurePath() {
        return path;
    }

    @Override
    public Optional<StructureMarker> getParent() {
        return Optional.ofNullable(parent);
    }

    public DefaultStructureMarker child(String structure) {
        return new DefaultStructureMarker(this, structure);
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
