package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.Ranges.copy;

class DefaultItemExporter implements ItemExporter {

    private final DataProcessor dataFormatter;

    public DefaultItemExporter(DataProcessor dataProcessor) {
        this.dataFormatter = dataProcessor;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        dataFormatter.startItem(item);
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = item.getReadingContainer().getReadings(exportInterval, item.getReadingType());
        if (!readings.isEmpty()) {
            Optional<Instant> lastExported = dataFormatter.processData(asMeterReading(item, readings));
            lastExported.ifPresent(item::setLastExportedDate);
        }
        dataFormatter.endItem(item);
        return exportInterval;
    }

    private Range<Instant> determineExportInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return item.getLastExportedDate()
                .map(last -> occurrence.getTask().getStrategy().isExportContinuousData() ? copy(occurrence.getExportedDataInterval()).withOpenLowerBound(last) : occurrence.getExportedDataInterval())
                .orElse(occurrence.getExportedDataInterval());
    }

    private MeterReadingImpl asMeterReading(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(Reading.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(reading),
                        (mr1, mr2) -> mr1.addAllReadings(mr2.getReadings())
                );
    }

    private MeterReadingImpl getMeterReadingWithIntervalBlock(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(buildIntervalBlock(item, readings));
        return meterReading;
    }

    private IntervalBlockImpl buildIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(IntervalReading.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(reading),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }


}
