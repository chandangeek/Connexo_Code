package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FormattedExportData;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class CompositeDataExportDestination implements DataExportDestination {

    private final List<DataExportDestination> components;

    public CompositeDataExportDestination(List<DataExportDestination> components) {
        this.components = ImmutableList.copyOf(components);
    }

    @Override
    public ExportTask getTask() {
        return components.stream()
                .map(DataExportDestination::getTask)
                .findAny()
                .orElse(null);
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void save() {
        components.forEach(DataExportDestination::save);
    }

    @Override
    public void send(List<FormattedExportData> data) {
        components.forEach(component -> component.send(data));
    }
}
