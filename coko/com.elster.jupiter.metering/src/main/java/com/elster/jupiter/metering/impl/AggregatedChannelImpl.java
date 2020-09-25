/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.CalculatedReadingRecord;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.streams.ExtraCollectors;
import com.elster.jupiter.util.time.RangeBuilder;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;


public class AggregatedChannelImpl implements ChannelContract, AggregatedChannel {

    private final DataAggregationService dataAggregationService;
    private Clock clock;

    private ChannelContract persistedChannel;
    private ReadingTypeDeliverable deliverable;
    private UsagePoint usagePoint;
    private MetrologyContract metrologyContract;
    private ChannelsContainer channelsContainer;

    @Inject
    public AggregatedChannelImpl(DataAggregationService dataAggregationService, Clock clock) {
        this.dataAggregationService = dataAggregationService;
        this.clock = clock;
    }

    public AggregatedChannelImpl init(ChannelContract channel,
                                      ReadingTypeDeliverable deliverable,
                                      UsagePoint usagePoint,
                                      MetrologyContract metrologyContract,
                                      ChannelsContainer channelsContainer) {
        this.persistedChannel = channel;
        this.deliverable = deliverable;
        this.usagePoint = usagePoint;
        this.metrologyContract = metrologyContract;
        this.channelsContainer = channelsContainer;
        return this;
    }

    @Override
    public long getId() {
        return persistedChannel.getId();
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return persistedChannel.getChannelsContainer();
    }

    @Override
    public Optional<TemporalAmount> getIntervalLength() {
        return persistedChannel.getIntervalLength();
    }

    @Override
    public boolean isRegular() {
        return persistedChannel.isRegular();
    }

    @Override
    public ReadingType getMainReadingType() {
        return persistedChannel.getMainReadingType();
    }

    @Override
    public Optional<? extends ReadingType> getBulkQuantityReadingType() {
        return Optional.empty(); // Aggregated channel was designed for single reading type
    }

    @Override
    public long getVersion() {
        return persistedChannel.getVersion();
    }

    @Override
    public boolean hasMacroPeriod() {
        return persistedChannel.hasMacroPeriod();
    }

    @Override
    public boolean hasData() {
        return persistedChannel.hasData();
    }

    @Override
    public List<Instant> toList(Range<Instant> range) {
        return this.persistedChannel.toList(range);
    }

    @Override
    public ZoneId getZoneId() {
        return persistedChannel.getZoneId();
    }

    @Override
    public void updateZoneId(ZoneId zoneId) {
        persistedChannel.updateZoneId(zoneId);
    }

    @Override
    public long getOffset() {
        return persistedChannel.getOffset();
    }

    @Override
    public void updateOffset(long offset) {
        persistedChannel.updateOffset(offset);
    }

    @Override
    public Optional<CimChannel> getCimChannel(ReadingType readingType) {
        if (!getMainReadingType().equals(readingType)) {
            return Optional.empty();
        }
        return persistedChannel.getCimChannel(readingType);
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval) {
        Map<Instant, AggregatedIntervalReadingRecord> calculatedReadings = getCalculatedIntervalReadings(interval);
        Map<Instant, AggregatedIntervalReadingRecord> persistedReadings =
                getPersistedIntervalReadings(interval)
                        .stream()
                        .map(AggregatedReadingIntervalRecordBackedByPersistentIntervalReadingRecord::new)
                        .collect(
                                Collectors.toMap(
                                        IntervalReadingRecord::getTimeStamp,
                                        Function.identity()));

        // return elements, sorted by timestamp
        Map<Instant, IntervalReadingRecord> orderedReadings = new TreeMap<>(calculatedReadings);
        orderedReadings.putAll(persistedReadings);

        return new ArrayList<>(orderedReadings.values());
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval) {
        if (!getMainReadingType().equals(readingType)) {
            throw new IllegalArgumentException("Incorrect reading type. This channel supports only " + this.deliverable.getReadingType().getMRID());
        }
        return getIntervalReadings(interval);
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(Range<Instant> interval) {
        Map<Instant, ReadingRecord> calculatedReadings =
                getCalculatedRegisterReadings(
                        interval,
                        record -> new AggregatedReadingRecordImpl(this.persistedChannel, record));
        Map<Instant, ReadingRecord> persistedReadings =
                getPersistedRegisterReadings(interval)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        BaseReadingRecord::getTimeStamp,
                                        Function.identity()));
        calculatedReadings.putAll(persistedReadings);
        return new ArrayList<>(calculatedReadings.values());
    }

    @Override
    public List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval) {
        if (!getMainReadingType().equals(readingType)) {
            throw new IllegalArgumentException("Incorrect reading type. This channel supports only " + this.deliverable.getReadingType().getMRID());
        }
        return getReadings(interval);
    }

    @Override
    public List<BaseReadingRecord> getReadings(Range<Instant> interval) {
        if (this.isRegular()) {
            Map<Instant, AggregatedIntervalReadingRecord> calculatedReadings = getCalculatedIntervalReadings(interval);
            Map<Instant, AggregatedIntervalReadingRecord> persistedReadings =
                    this.persistedChannel
                            .getReadings(interval)
                            .stream()
                            .map(AggregatedReadingIntervalRecordBackedByPersistentBaseReadingRecord::new)
                            .collect(
                                    Collectors.toMap(
                                            BaseReadingRecord::getTimeStamp,
                                            Function.identity()));
            calculatedReadings.putAll(persistedReadings);
            return new ArrayList<>(calculatedReadings.values());
        } else {
            Map<Instant, BaseReadingRecord> calculatedReadings = getCalculatedRegisterReadings(interval, BaseReadingRecord.class::cast);
            Map<Instant, BaseReadingRecord> persistedReadings =
                    this.persistedChannel
                            .getReadings(interval)
                            .stream()
                            .collect(
                                    Collectors.toMap(
                                            BaseReadingRecord::getTimeStamp,
                                            Function.identity()));
            calculatedReadings.putAll(persistedReadings);
            return new ArrayList<>(calculatedReadings.values());
        }
    }

    private Map<Instant, AggregatedIntervalReadingRecord> getCalculatedIntervalReadings(Range<Instant> interval) {
        if (isMetrologyConfigurationActive(interval)) {
            return this.dataAggregationService
                    .calculate(usagePoint, metrologyContract, interval)
                    .getCalculatedDataFor(this.deliverable).stream()
                    .map(record -> new AggregatedReadingRecordImpl(this.persistedChannel, record))
                    .collect(Collectors.toMap(
                            AggregatedIntervalReadingRecord::getTimeStamp,
                            Function.identity()));
        } else {
            return new HashMap<>();
        }
    }

    private <T extends BaseReadingRecord> Map<Instant, T> getCalculatedRegisterReadings(Range<Instant> interval, Function<CalculatedReadingRecord, T> mapper) {
        if (isMetrologyConfigurationActive(interval)) {
            return this.dataAggregationService
                    .calculate(usagePoint, metrologyContract, interval)
                    .getCalculatedDataFor(this.deliverable)
                    .stream()
                    .map(mapper::apply)
                    .collect(
                            Collectors.toMap(
                                    BaseReading::getTimeStamp,
                                    Function.identity()));
        } else {
            return new HashMap<>();
        }
    }

    private boolean isMetrologyConfigurationActive(Range<Instant> interval) {
        return usagePoint.getEffectiveMetrologyConfigurations(interval).stream()
                .anyMatch(emc -> emc.getMetrologyConfiguration().getContracts().contains(metrologyContract));
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval) {
        if (!getMainReadingType().equals(readingType)) {
            throw new IllegalArgumentException("Incorrect reading type. This channel supports only " + this.deliverable.getReadingType().getMRID());
        }
        return getRegisterReadings(interval);
    }

    @Override
    public List<? extends BaseReadingRecord> getJournaledChannelReadings(ReadingType readingType, Range<Instant> interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        List<BaseReadingRecord> journaledData = getTimeSeries().getJournalEntries(interval).stream()
                .map(entry -> new JournaledChannelReadingRecordImpl(this, entry))
                .map(reading -> reading.filter(readingType))
                .collect(Collectors.toList());
        journaledData.addAll(getCalculatedIntervalReadings(interval).values());

        return journaledData;
    }

    @Override
    public List<? extends ReadingRecord> getJournaledRegisterReadings(ReadingType readingType, Range<Instant> interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        List<ReadingRecord> aggregatedDataToMerge = getCalculatedRegisterReadings(interval);
        List<? extends ReadingRecord> journaledData = getTimeSeries().getJournalEntries(interval).stream()
                .map(entry -> new JournaledRegisterReadingRecordImpl(this, entry))
                .map(reading -> reading.filter(readingType))
                .collect(ExtraCollectors.toImmutableList());
        aggregatedDataToMerge.addAll(journaledData);

        return aggregatedDataToMerge;
    }

    @Override
    public Optional<BaseReadingRecord> getReading(Instant when) {
        Range<Instant> requestRange;
        if (isRegular()) {
            requestRange = Range.open(persistedChannel.getPreviousDateTime(when), persistedChannel.getNextDateTime(when));
        } else {
            requestRange = Range.open(when.minusSeconds(1), when.plusSeconds(1));
        }

        return getReadings(requestRange).stream().findFirst();
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, BaseReading baseReading) {
        return persistedChannel.createReadingQuality(type, readingType, baseReading);
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, Instant timestamp) {
        return persistedChannel.createReadingQuality(type, readingType, timestamp);
    }

    @Override
    public List<ReadingQualityRecord> createReadingQualityForRecords(ReadingQualityType type, ReadingType readingType, List<BaseReadingRecord> records) {
        return persistedChannel.createReadingQualityForRecords(type, readingType, records);
    }

    @Override
    public List<ReadingQualityRecord> createReadingQualityForTimestamps(ReadingQualityType type, ReadingType readingType, List<Instant> timestamps) {
        return persistedChannel.createReadingQualityForTimestamps(type, readingType, timestamps);
    }

    @Override
    public ReadingQualityFetcher findReadingQualities() {
        return persistedChannel.findReadingQualities();
    }

    @Override
    public List<BaseReadingRecord> getReadingsUpdatedSince(ReadingType readingType, Range<Instant> interval, Instant since) {
        return getReadings(readingType, interval).stream()
                .filter(reading -> reading.getReportedDateTime() != null).filter(reading -> reading.getReportedDateTime().isAfter(since))
                .collect(Collectors.toList());
    }

    @Override
    public List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount) {
        if (isRegular()) {
            Instant from = when;
            for (int i = 0; i <= readingCount; i++) {
                from = persistedChannel.getPreviousDateTime(from);
            }
            return getReadings(Range.openClosed(from, persistedChannel.getPreviousDateTime(when)));
        } else {
            RangeBuilder rangeBuilder = new RangeBuilder();
            this.dataAggregationService.introspect(this.usagePoint, this.metrologyContract, this.channelsContainer.getRange()).getChannelUsagesFor(this.deliverable)
                    .stream()
                    .flatMap(channelUsage -> channelUsage.getChannel()
                            .getReadingsBefore(when, readingCount)
                            .stream()
                            .filter(readingRecord -> channelUsage.getRange().contains(readingRecord.getTimeStamp())))
                    .filter(readingRecord -> readingRecord.getTimeStamp().isBefore(when))
                    .forEach(record -> rangeBuilder.add(record.getTimeStamp()));
            if (rangeBuilder.hasRange()) {
                Range<Instant> range = rangeBuilder.getRange();
                if (range.hasLowerBound()) {
                    range = Range.openClosed(range.lowerEndpoint().minusSeconds(1), range.upperEndpoint());
                }
                List<BaseReadingRecord> readingRecords = getReadings(range);
                return readingRecords.subList(Math.max(0, readingRecords.size() - readingCount), readingRecords.size());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount) {
        if (isRegular()) {
            Instant from = when;
            for (int i = 0; i < readingCount; i++) {
                from = persistedChannel.getPreviousDateTime(from);
            }
            return getReadings(Range.openClosed(from, when));
        } else {
            RangeBuilder rangeBuilder = new RangeBuilder();
            rangeBuilder.add(when);
            this.dataAggregationService.introspect(this.usagePoint, this.metrologyContract, this.channelsContainer.getRange()).getChannelUsagesFor(this.deliverable)
                    .stream()
                    .flatMap(channelUsage -> channelUsage.getChannel()
                            .getReadingsBefore(when, readingCount)
                            .stream()
                            .filter(readingRecord -> channelUsage.getRange().contains(readingRecord.getTimeStamp())))
                    .filter(readingRecord -> readingRecord.getTimeStamp().isBefore(when))
                    .forEach(record -> rangeBuilder.add(record.getTimeStamp()));
            if (rangeBuilder.hasRange()) {
                Range<Instant> range = rangeBuilder.getRange();
                if (range.hasLowerBound() && range.hasUpperBound()) {
                    range = Range.openClosed(range.lowerEndpoint().minusSeconds(1), range.upperEndpoint().plusSeconds(1));
                }
                List<BaseReadingRecord> readingRecords = getReadings(range);
                return readingRecords.subList(Math.max(0, readingRecords.size() - readingCount), readingRecords.size());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public void editReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        persistedChannel.editReadings(system, readings);
    }

    @Override
    public void confirmReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        persistedChannel.confirmReadings(system, readings);
    }

    @Override
    public void estimateReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        persistedChannel.estimateReadings(system, readings);
    }

    @Override
    public void removeReadings(QualityCodeSystem system, List<? extends BaseReadingRecord> readings) {
        persistedChannel.removeReadings(system, readings);
    }

    @Override
    public Instant getFirstDateTime() {
        Instant persistedChannelFirstDateTime = this.persistedChannel.getFirstDateTime();
        CalculatedMetrologyContractData calculatedMetrologyContractData = this.dataAggregationService.calculate(this.usagePoint,
                this.metrologyContract, this.channelsContainer.getRange());
        if (!calculatedMetrologyContractData.isEmpty()) {
            List<? extends BaseReadingRecord> deliverableData = calculatedMetrologyContractData.getCalculatedDataFor(this.deliverable);
            if (!deliverableData.isEmpty()) {
                Instant calculatedFirstDateTime = deliverableData.get(0).getTimeStamp();
                if (calculatedFirstDateTime.compareTo(persistedChannelFirstDateTime) < 0) {
                    return calculatedFirstDateTime;
                } else {
                    return persistedChannelFirstDateTime;
                }
            }
        }
        return persistedChannelFirstDateTime;
    }

    @Override
    public Instant getLastDateTime() {
        Instant persistedChannelLastDateTime = this.persistedChannel.getLastDateTime();
        MetrologyContractCalculationIntrospector introspect = this.dataAggregationService.introspect(this.usagePoint, this.metrologyContract, this.channelsContainer.getRange());
        Optional<Instant> max = introspect.getChannelUsagesFor(this.deliverable)
                .stream()
                .map(MetrologyContractCalculationIntrospector.ChannelUsage::getChannel)
                .map(Channel::getLastDateTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        return max.filter(instant -> persistedChannelLastDateTime == null || instant.isAfter(persistedChannelLastDateTime))
                .orElse(persistedChannelLastDateTime);
    }

    @Override
    public Instant getNextDateTime(Instant instant) {
        return persistedChannel.getNextDateTime(instant);
    }

    @Override
    public Instant getPreviousDateTime(Instant instant) {
        return persistedChannel.getPreviousDateTime(instant);
    }

    @Override
    public Instant truncateToIntervalLength(Instant instant) {
        return persistedChannel.truncateToIntervalLength(instant);
    }

    @Override
    public MeterReading deleteReadings(Range<Instant> period) {
        return MeterReadingImpl.newInstance();
    }

    @Override
    public Object[] toArray(BaseReading reading, ReadingType readingType, ProcessStatus status) {
        return this.persistedChannel.toArray(reading, readingType, status);
    }

    @Override
    public Object[] toArray(BaseReadingRecord readingRecord) {
        return this.persistedChannel.toArray(readingRecord);
    }

    @Override
    public void validateValues(BaseReading reading, Object[] values) {
        persistedChannel.validateValues(reading, values);
    }

    @Override
    public TimeSeries getTimeSeries() {
        return persistedChannel.getTimeSeries();
    }

    @Override
    public DerivationRule getDerivationRule(IReadingType readingType) {
        return persistedChannel.getDerivationRule(readingType);
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values) {
        return persistedChannel.getTimePeriod(reading, values);
    }

    @Override
    public RecordSpecs getRecordSpecDefinition() {
        return persistedChannel.getRecordSpecDefinition();
    }

    @Override
    public List<IReadingType> getReadingTypes() {
        return persistedChannel.getReadingTypes();
    }

    private List<IntervalReadingRecord> getPersistedIntervalReadings(Range<Instant> interval) {
        return Collections.unmodifiableList(persistedChannel.getIntervalReadings(interval));
    }

    @Override
    public List<AggregatedIntervalReadingRecord> getAggregatedIntervalReadings(Range<Instant> interval) {
        Map<Instant, AggregatedIntervalReadingRecord> calculatedReadings = this.getCalculatedIntervalReadings(interval);
        Map<Instant, IntervalReadingRecord> persistedReadings = toMap(this.getPersistedIntervalReadings(interval));
        Map<Instant, AggregatedIntervalReadingRecord> merged =
                calculatedReadings
                        .entrySet()
                        .stream()
                        .map(entry -> this.merge(entry, persistedReadings))
                        .collect(Collectors.toMap(
                                AggregatedIntervalReadingRecord::getTimeStamp,
                                Function.identity()));
        persistedReadings
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(AggregatedReadingIntervalRecordBackedByPersistentIntervalReadingRecord::new)
                .forEach(reading -> merged.put(reading.getTimeStamp(), reading));
        return new ArrayList<>(merged.values());
    }

    private <T extends BaseReadingRecord> Map<Instant, T> toMap(List<T> readings) {
        return readings.stream().collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, Function.identity()));
    }

    private AggregatedIntervalReadingRecord merge(Map.Entry<Instant, AggregatedIntervalReadingRecord> calculated, Map<Instant, IntervalReadingRecord> persistedRecords) {
        IntervalReadingRecord persisted = persistedRecords.get(calculated.getKey());
        if (persisted == null) {
            return calculated.getValue();
        } else {
            persistedRecords.remove(calculated.getKey());
            return new EditedAggregatedReadingRecord(calculated.getValue(), persisted);
        }
    }

    @Override
    public List<ReadingRecord> getPersistedRegisterReadings(Range<Instant> interval) {
        return persistedChannel.getRegisterReadings(interval);
    }

    @Override
    public List<ReadingRecord> getCalculatedRegisterReadings(Range<Instant> interval) {
        return new ArrayList<>(getCalculatedRegisterReadings(interval, record -> new AggregatedReadingRecordImpl(this.persistedChannel, record)).values());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o != null
                && getClass() == o.getClass()
                && persistedChannel.equals(((AggregatedChannelImpl) o).persistedChannel);
    }

    @Override
    public int hashCode() {
        return persistedChannel.hashCode();
    }

    /**
     * Implementation class that only serves the purpose of unifying API in using
     * AggregatedIntervalReadingRecord instead of BaseReadingRecord.
     */
    private static class AggregatedReadingIntervalRecordBackedByPersistentBaseReadingRecord implements AggregatedIntervalReadingRecord {
        private final BaseReadingRecord persistentRecord;

        private AggregatedReadingIntervalRecordBackedByPersistentBaseReadingRecord(BaseReadingRecord persistentRecord) {
            this.persistentRecord = persistentRecord;
        }

        @Override
        public boolean wasEdited() {
            return true;
        }

        @Override
        public BigDecimal getOriginalValue() {
            return null;    // No original value since this record replace a calculated record that was missing
        }

        @Override
        public boolean isPartOfTimeOfUseGap() {
            return false;
        }

        @Override
        public Optional<Event> getTimeOfUseEvent() {
            return Optional.empty();
        }

        @Override
        public IntervalReadingRecord filter(ReadingType readingType) {
            return this;
        }

        @Override
        public List<Quantity> getQuantities() {
            return this.persistentRecord.getQuantities();
        }

        @Override
        public Quantity getQuantity(ReadingType readingType) {
            return persistentRecord.getQuantity(readingType);
        }

        @Override
        public Quantity getQuantity(int offset) {
            return persistentRecord.getQuantity(offset);
        }

        @Override
        public ReadingType getReadingType() {
            return persistentRecord.getReadingType();
        }

        @Override
        public ReadingType getReadingType(int offset) {
            return persistentRecord.getReadingType(offset);
        }

        @Override
        public List<? extends ReadingType> getReadingTypes() {
            return persistentRecord.getReadingTypes();
        }

        @Override
        public ProcessStatus getProcessStatus() {
            return persistentRecord.getProcessStatus();
        }

        @Override
        public void setProcessingFlags(ProcessStatus.Flag... flags) {
            persistentRecord.setProcessingFlags(flags);
        }

        @Override
        public List<? extends ReadingQualityRecord> getReadingQualities() {
            return persistentRecord.getReadingQualities();
        }

        @Override
        public boolean edited() {
            return persistentRecord.edited();
        }

        @Override
        public boolean wasAdded() {
            return persistentRecord.wasAdded();
        }

        @Override
        public boolean confirmed() {
            return persistentRecord.confirmed();
        }

        @Override
        public BigDecimal getSensorAccuracy() {
            return persistentRecord.getSensorAccuracy();
        }

        @Override
        public Instant getTimeStamp() {
            return persistentRecord.getTimeStamp();
        }

        @Override
        public Instant getReportedDateTime() {
            return persistentRecord.getReportedDateTime();
        }

        @Override
        public BigDecimal getValue() {
            return persistentRecord.getValue();
        }

        @Override
        public String getSource() {
            return persistentRecord.getSource();
        }

        @Override
        public Optional<Range<Instant>> getTimePeriod() {
            return persistentRecord.getTimePeriod();
        }

        @Override
        public boolean hasReadingQuality(ReadingQualityType readingQualityType) {
            return persistentRecord.hasReadingQuality(readingQualityType);
        }

    }

}
