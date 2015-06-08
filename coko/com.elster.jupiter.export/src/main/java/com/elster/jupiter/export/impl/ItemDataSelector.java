package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;

import java.util.Optional;

interface ItemDataSelector {
    Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item);
}
