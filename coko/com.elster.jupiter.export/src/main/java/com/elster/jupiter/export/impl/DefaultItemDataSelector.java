package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;

class DefaultItemDataSelector implements ItemDataSelector {

    private final Clock clock;

    public DefaultItemDataSelector(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = item.getReadingContainer().getReadings(exportInterval, item.getReadingType());

        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            return Optional.of(new MeterReadingData(item, meterReading, structureMarker(item, readings.get(0).getTimeStamp())));
        }
        return Optional.empty();
    }

    private StructureMarker structureMarker(IReadingTypeDataExportItem item, Instant instant) {
        return DefaultStructureMarker.createRoot(clock, item.getReadingContainer().getMeter(instant).map(Meter::getMRID).orElse(""))
                .child(item.getReadingContainer().getUsagePoint(instant).map(UsagePoint::getMRID).orElse(""))
                .child(item.getReadingType().getMRID() == null ? "" : item.getReadingType().getMRID());
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
