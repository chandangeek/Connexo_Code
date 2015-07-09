package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

class CompositeDataExportDestination implements Destination {

    private final List<Destination> components;

    public CompositeDataExportDestination(List<? extends Destination> components) {
        this.components = ImmutableList.copyOf(components);
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory) {
        components.forEach(component -> component.send(files, tagReplacerFactory));
    }
}
