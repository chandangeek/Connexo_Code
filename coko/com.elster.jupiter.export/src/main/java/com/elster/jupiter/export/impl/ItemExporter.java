package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.MeterReadingData;

public interface ItemExporter {

    FormattedData exportItem(DataExportOccurrence occurrence, MeterReadingData item);

    void done();

}
