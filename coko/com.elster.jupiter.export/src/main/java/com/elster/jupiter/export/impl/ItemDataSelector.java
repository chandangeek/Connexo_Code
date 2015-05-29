package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 21/05/2015
 * Time: 13:46
 */
public interface ItemDataSelector {
    Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item);
}
