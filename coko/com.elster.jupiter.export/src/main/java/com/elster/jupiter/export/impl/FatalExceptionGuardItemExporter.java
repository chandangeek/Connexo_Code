package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.MeterReadingData;
import com.google.common.collect.Range;

import java.time.Instant;

class FatalExceptionGuardItemExporter implements ItemExporter {

    private final ItemExporter decorated;

    public FatalExceptionGuardItemExporter(ItemExporter decorated) {
        this.decorated = decorated;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData item) {
        try {
            return decorated.exportItem(occurrence, item);
        } catch (DataExportException | FatalDataExportException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new FatalDataExportException(e);
        }
    }

    @Override
    public void done() {
        decorated.done();
    }
}
