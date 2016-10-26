package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.AggregatedDataSelectorConfig;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.EventDataSelector;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.logging.Logger;

interface IStandardDataSelector extends StandardDataSelector, EventDataSelector, AggregatedDataSelectorConfig {

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);

    DataSelector asReadingTypeDataSelector(Logger logger, Thesaurus thesaurus);

    DataSelector asEventDataSelector(Logger logger, Thesaurus thesaurus);

    DataSelector asAggregatedDataSelector(Logger logger, Thesaurus thesaurus);

    IReadingTypeDataExportItem addExportItem(ReadingContainer readingContainer, ReadingType readingType);

    void delete();
}
