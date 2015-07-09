package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;

import java.util.List;

interface ItemExporter {

    List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData item);

    void done();

}
