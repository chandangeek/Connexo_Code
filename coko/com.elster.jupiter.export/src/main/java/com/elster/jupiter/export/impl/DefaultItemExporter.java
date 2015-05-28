package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;

class DefaultItemExporter implements ItemExporter {

    private final DataProcessor dataFormatter;
    private final List<IReadingTypeDataExportItem> exportItems = new ArrayList<>();

    public DefaultItemExporter(DataProcessor dataProcessor) {
        this.dataFormatter = dataProcessor;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        IReadingTypeDataExportItem item = (IReadingTypeDataExportItem) meterReadingData.getItem();
        dataFormatter.startItem(item);
        item.setLastRun(occurrence.getTriggerTime());
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);
        Optional<Instant> lastExported = dataFormatter.processData(meterReadingData);
        lastExported.ifPresent(item::setLastExportedDate);
        dataFormatter.endItem(item);
        exportItems.add(item);
        return exportInterval;
    }

    @Override
    public void done() {
        exportItems.forEach(IReadingTypeDataExportItem::update);
    }

    private Range<Instant> determineExportInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return occurrence.getTask().getReadingTypeDataSelector()
                .map(IReadingTypeDataSelector.class::cast)
                .map(selector -> selector.adjustedExportPeriod(occurrence, item))
                .orElse(occurrence.getExportedDataInterval());
    }

    private MeterReadingImpl asMeterReading(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(item, readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(ReadingRecord.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(forReadingType(reading, item.getReadingType())),
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
                .map(IntervalReadingRecord.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(forReadingType(reading, item.getReadingType())),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }

    private IntervalReading forReadingType(IntervalReadingRecord readingRecord, ReadingType readingType) {
        return intervalReading(readingRecord, readingType);
    }

    private Reading forReadingType(ReadingRecord readingRecord, ReadingType readingType) {
        return reading(readingRecord, readingType);
    }

}
