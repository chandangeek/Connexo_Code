package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 26/11/2014
 * Time: 12:44
 */
public interface IDataExportOccurrenceFinder {
    IDataExportOccurrenceFinder setStart(Integer start);

    IDataExportOccurrenceFinder setLimit(Integer limit);

    IDataExportOccurrenceFinder withStartDateIn(Range<Instant> interval);

    IDataExportOccurrenceFinder withEndDateIn(Range<Instant> interval);

    IDataExportOccurrenceFinder withExportPeriodContaining(Instant timeStamp);

    List<? extends DataExportOccurrence> find();
}
