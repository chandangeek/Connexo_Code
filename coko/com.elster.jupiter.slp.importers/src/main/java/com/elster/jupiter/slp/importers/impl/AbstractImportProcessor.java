/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import java.time.Clock;
import java.util.Optional;

public abstract class AbstractImportProcessor<T extends FileImportRecord> implements FileImportProcessor<T> {

    private final SyntheticLoadProfileDataImporterContext context;

    protected AbstractImportProcessor(SyntheticLoadProfileDataImporterContext context) {
        this.context = context;
    }

    protected SyntheticLoadProfileDataImporterContext getContext() {
        return context;
    }
}
