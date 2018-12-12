/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class CompositeDataExportDestination implements Destination {

    private final List<Destination> components;

    public CompositeDataExportDestination(List<? extends Destination> components) {
        this.components = ImmutableList.copyOf(components);
    }

    @Override
    public void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus) {
        components.forEach(component -> component.send(files, tagReplacerFactory, logger, thesaurus));
    }
}
