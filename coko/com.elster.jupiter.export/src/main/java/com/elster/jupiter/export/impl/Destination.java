/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

interface Destination {
    void send(Map<StructureMarker, Path> files, TagReplacerFactory tagReplacerFactory, Logger logger, Thesaurus thesaurus);
}
