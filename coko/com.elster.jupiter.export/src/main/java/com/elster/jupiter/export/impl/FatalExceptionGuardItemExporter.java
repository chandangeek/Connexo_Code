package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;

import java.util.List;

class FatalExceptionGuardItemExporter implements ItemExporter {

    private final ItemExporter decorated;

    public FatalExceptionGuardItemExporter(ItemExporter decorated) {
        this.decorated = decorated;
    }

    @Override
    public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData item) {
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
