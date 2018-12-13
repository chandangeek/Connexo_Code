/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class CompositeDataExportDestination implements FormattedFileDestination, DataDestination {

    private final List<FormattedFileDestination> fileDestinations;
    private final List<DataDestination> dataDestinations;

    public CompositeDataExportDestination(Destination... components) {
        this(Arrays.asList(components));
    }

    public CompositeDataExportDestination(List<? extends Destination> components) {
        fileDestinations = new ArrayList<>(components.size());
        dataDestinations = new ArrayList<>(components.size());
        components.forEach(component -> {
            switch (component.getType()) {
                case DATA:
                    dataDestinations.add((DataDestination) component);
                    break;
                case FILE:
                    fileDestinations.add((FormattedFileDestination) component);
                    break;
                case COMPOSITE:
                    dataDestinations.addAll(((CompositeDataExportDestination) component).dataDestinations);
                    fileDestinations.addAll(((CompositeDataExportDestination) component).fileDestinations);
                    break;
            }
        });
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
        fileDestinations.forEach(component -> component.send(files, tagReplacerFactory, logger, thesaurus));
    }

    @Override
    public void send(List<ExportData> data, TagReplacerFactory tagReplacerFactory, Logger logger) {
        dataDestinations.forEach(component -> component.send(data, tagReplacerFactory, logger));
    }

    public void send(List<ExportData> data, Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
        send(files, tagReplacerFactory, logger, thesaurus);
        send(data, tagReplacerFactory, logger);
    }

    public boolean hasFileDestinations() {
        return !fileDestinations.isEmpty();
    }

    public boolean hasDataDestinations() {
        return !dataDestinations.isEmpty();
    }

    @Override
    public Type getType() {
        return Type.COMPOSITE;
    }
}
