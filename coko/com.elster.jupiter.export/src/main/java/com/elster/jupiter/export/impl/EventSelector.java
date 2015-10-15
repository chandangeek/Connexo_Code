package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.EventDataSelector;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class EventSelector implements DataSelector {

    private final DataExportService dataExportService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final Clock clock;

    private EventDataSelector selector;
    private Logger logger;
    private Counter devicesWithEvents = Counters.newStrictCounter();
    private Counter events = Counters.newStrictCounter();

    @Inject
    EventSelector(TransactionService transactionService, DataExportService dataExportService, Clock clock, Thesaurus thesaurus) {
        this.transactionService = transactionService;
        this.dataExportService = dataExportService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    static EventSelector from(DataModel dataModel, EventDataSelector readingTypeDataSelector, Logger logger) {
        return dataModel.getInstance(EventSelector.class).init(readingTypeDataSelector, logger);
    }

    private EventSelector init(EventDataSelector selector, Logger logger) {
        this.selector = selector;
        this.logger = logger;
        return this;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        Range<Instant> range = determineRange(occurrence);
        if (!range.hasUpperBound() || clock.instant().isBefore(range.upperEndpoint())) {
            try (TransactionContext context = transactionService.getContext()) {
                MessageSeeds.EXPORT_PERIOD_COVERS_FUTURE.log(logger, thesaurus, selector.getExportPeriod().getName());
                context.commit();
            }
        }
        Stream<ExportData> exportDataStream = getExportDataStream(range);
        ((IDataExportOccurrence) occurrence).summarize(buildSummary());

        return exportDataStream;
    }

    private String buildSummary() {
        return thesaurus.getFormat(TranslationKeys.NUMBER_OF_DEVICES_WITH_EVENTS_SUCCESSFULLY_EXPORTED)
                .format(devicesWithEvents.getValue()) +
                System.getProperty("line.separator") +
                thesaurus.getFormat(TranslationKeys.NUMBER_OF_EVENTS_EXPORTED)
                        .format(events.getValue());
    }

    private Stream<ExportData> getExportDataStream(Range<Instant> range) {
        return selector.getEndDeviceGroup()
                .getMembers(range)
                .stream()
                .map(EndDeviceMembership::getEndDevice)
                .map(endDevice -> buildEventData(endDevice, range));
    }

    private MeterEventData buildEventData(EndDevice endDevice, Range<Instant> range) {
        MeterReadingImpl meterReading = buildMeterReading(endDevice, range);
        StructureMarker structureMarker = buildStructureMarker(endDevice, range);
        return new MeterEventData(meterReading, structureMarker);
    }

    private StructureMarker buildStructureMarker(EndDevice endDevice, Range<Instant> range) {
        return dataExportService.forRoot(endDevice.getMRID()).withPeriod(range);
    }

    private MeterReadingImpl buildMeterReading(EndDevice endDevice, Range<Instant> range) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addAllEndDeviceEvents(endDevice.getDeviceEvents(range).stream()
                .filter(selector.getFilterPredicate())
                .map(endDeviceEventRecord -> EndDeviceEventImpl.of(endDeviceEventRecord.getEventTypeCode(), endDeviceEventRecord.getCreatedDateTime()))
                .collect(Collectors.toList()));
        events.add(meterReading.getEvents().size());
        devicesWithEvents.add(meterReading.getEvents().size() == 0 ? 0 : 1);
        return meterReading;
    }

    private Range<Instant> determineRange(DataExportOccurrence occurrence) {
        Range<Instant> trivialRange = trivialRange(occurrence);
        if (selector.getEventStrategy().isExportContinuousData()) {
            return lastSuccessfulOccurrence(occurrence)
                    .flatMap(DataExportOccurrence::getDefaultSelectorOccurrence)
                    .map(DefaultSelectorOccurrence::getExportedDataInterval)
                    .map(Range::upperEndpoint)
                    .filter(upperEndPoint -> upperEndPoint.isBefore(trivialRange.lowerEndpoint()))
                    .map(upperEndPoint -> Range.openClosed(upperEndPoint, trivialRange.upperEndpoint()))
                    .orElse(trivialRange);
        }
        return trivialRange;
    }

    private Range<Instant> trivialRange(DataExportOccurrence occurrence) {
        return occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.<Instant>all());
    }

    private Optional<DataExportOccurrence> lastSuccessfulOccurrence(DataExportOccurrence occurrence) {
        return occurrence.getTask().getOccurrencesFinder()
                .withStartDateIn(Range.lessThan(occurrence.getStartDate().get()))
                .stream()
                .filter(occ -> DataExportStatus.SUCCESS.equals(occ.getStatus()))
                .max(Comparator.comparing(occ -> occ.getDefaultSelectorOccurrence().get().getExportedDataInterval().upperEndpoint()));
    }

}
