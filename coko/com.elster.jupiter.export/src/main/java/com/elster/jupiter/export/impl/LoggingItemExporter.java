package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;

class LoggingItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final Logger logger;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final DateTimeFormatter timeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault());

    public LoggingItemExporter(Thesaurus thesaurus, TransactionService transactionService, Logger logger, ItemExporter decorated) {
        this.transactionService = transactionService;
        this.logger = logger;
        this.decorated = decorated;
        this.thesaurus = thesaurus;
    }

    @Override
    public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        ReadingTypeDataExportItem item = meterReadingData.getItem();
        String mrid = item.getReadingContainer().getMeter(occurrence.getTriggerTime()).map(Meter::getMRID).orElse("");
        String readingType = item.getReadingType().getAliasName();
        try {
            List<FormattedExportData> data = decorated.exportItem(occurrence, meterReadingData);
            Range<Instant> range = determineExportInterval(occurrence, item);
            String fromDate = range.hasLowerBound() ? timeFormatter.format(range.lowerEndpoint()) : "";
            String toDate = range.hasUpperBound() ? timeFormatter.format(range.upperEndpoint()) : "";
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_EXPORTED_SUCCESFULLY.log(logger, thesaurus, mrid, readingType, fromDate, toDate)));
            return data;
        } catch (DataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FAILED.log(logger, thesaurus, e.getCause(), mrid, readingType)));
            throw e;
        } catch (FatalDataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FATALLY_FAILED.log(logger, thesaurus, e.getCause(), mrid, readingType)));
            throw e;
        }
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

    @Override
    public void done() {
        decorated.done();
    }
}
