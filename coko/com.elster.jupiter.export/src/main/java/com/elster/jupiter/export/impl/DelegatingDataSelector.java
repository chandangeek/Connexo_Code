/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.ExportData;

import java.util.logging.Logger;
import java.util.stream.Stream;

class DelegatingDataSelector implements DataSelector {

    private final Logger logger;

    DelegatingDataSelector(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
        return dataExportOccurrence.getTask().getStandardDataSelectorConfig()
                .map(selectorConfig -> selectorConfig.createDataSelector(logger))
                .orElseThrow(IllegalStateException::new)
                .selectData(dataExportOccurrence);
    }
}
