package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.FormattedExportData;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class CompositeDataExportDestination implements Destination {

    private final List<Destination> components;

    public CompositeDataExportDestination(List<Destination> components) {
        this.components = ImmutableList.copyOf(components);
    }

    public void send(List<FormattedExportData> data) {
        components.forEach(component -> component.send(data));
    }
}
