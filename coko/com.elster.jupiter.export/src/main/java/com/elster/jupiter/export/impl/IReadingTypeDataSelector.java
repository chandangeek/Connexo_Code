package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.logging.Logger;

interface IReadingTypeDataSelector extends ReadingTypeDataSelector {

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);

    DataSelector asDataSelector(Logger logger, Thesaurus thesaurus);

    void delete();

    IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType);
}
