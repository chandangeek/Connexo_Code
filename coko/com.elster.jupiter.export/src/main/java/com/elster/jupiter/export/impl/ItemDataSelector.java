package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;

import java.time.Instant;
import java.util.Optional;

interface ItemDataSelector {
    Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item);

    Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since);
}
