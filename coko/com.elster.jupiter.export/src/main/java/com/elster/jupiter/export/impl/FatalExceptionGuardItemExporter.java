package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.google.common.collect.Range;

import java.time.Instant;

public class FatalExceptionGuardItemExporter implements ItemExporter {

    private final ItemExporter decorated;

    public FatalExceptionGuardItemExporter(ItemExporter decorated) {
        this.decorated = decorated;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        try {
            return decorated.exportItem(occurrence, item);
        } catch (RuntimeException e) {
            throw new FatalDataExportException(e);
        }
    }
}
