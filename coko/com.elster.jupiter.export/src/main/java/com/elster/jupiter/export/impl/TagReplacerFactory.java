package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;

public interface TagReplacerFactory {

    TagReplacer forMarker(StructureMarker structureMarker);
}
