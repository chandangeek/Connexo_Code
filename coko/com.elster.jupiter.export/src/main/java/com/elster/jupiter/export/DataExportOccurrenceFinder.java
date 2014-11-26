package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 26/11/2014
 * Time: 12:44
 */
public interface DataExportOccurrenceFinder {
    DataExportOccurrenceFinder setStart(Integer start);

    DataExportOccurrenceFinder setLimit(Integer limit);

    DataExportOccurrenceFinder withStartDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withEndDateIn(Range<Instant> interval);

    DataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp);

    List<? extends DataExportOccurrence> find();
}
