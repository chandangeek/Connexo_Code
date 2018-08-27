/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.ReadingDataFormatter;
import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.util.logging.Logger;
import java.util.stream.Stream;

class NullDataFormatter implements ReadingDataFormatter {
    @Override
    public void startExport(DataExportOccurrence dataExportOccurrence, Logger logger) {
    }

    @Override
    public void startItem(ReadingTypeDataExportItem item) {
    }

    @Override
    public FormattedData processData(Stream<ExportData> exportData) {
        throw new IllegalStateException("No data formatter is found to format the data.");
    }

    @Override
    public void endItem(ReadingTypeDataExportItem item) {
    }

    @Override
    public void endExport() {
    }
}
