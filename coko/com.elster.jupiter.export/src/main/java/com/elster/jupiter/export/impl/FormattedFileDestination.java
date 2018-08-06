/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

interface FormattedFileDestination extends Destination {
    void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus);

    @Override
    default Type getType() {
        return Type.FILE;
    }
}
