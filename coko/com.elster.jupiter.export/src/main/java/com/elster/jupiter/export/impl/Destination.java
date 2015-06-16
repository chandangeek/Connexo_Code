package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;

import java.nio.file.Path;
import java.util.Map;

interface Destination {
    void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory);
}
