package com.elster.jupiter.metering.impl;

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
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AggregatedChannelImpl implements ChannelContract, AggregatedChannel {

    private final DataAggregationService dataAggregationService;

    private ChannelContract persistedChannel;
    private ReadingTypeDeliverable deliverable;
    private UsagePoint usagePoint;
    private MetrologyContract metrologyContract;
    private ChannelsContainer channelsContainer;

    @Inject
    public AggregatedChannelImpl(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
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
        return persistedChannel.toList(range);
    }

    @Override
    public ZoneId getZoneId() {
        return persistedChannel.getZoneId();
    }

    @Override
    public Optional<CimChannel> getCimChannel(ReadingType readingType) {
        if (!getMainReadingType().equals(readingType)) {
            throw new IllegalArgumentException("Incorrect reading type. This channel supports only " + this.deliverable.getReadingType().getMRID());
        }
        return persistedChannel.getCimChannel(readingType);
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval) {
        Map<Instant, IntervalReadingRecord> readings = getCalculatedReadings(interval, record -> new CalculatedReadingRecordImpl(this.persistedChannel, record));
        readings.putAll(persistedChannel.getIntervalReadings(interval).stream()
                .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                        .identity())));
        return new ArrayList<>(readings.values());
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
        Map<Instant, ReadingRecord> readings = getCalculatedReadings(interval, record -> new CalculatedReadingRecordImpl(this.persistedChannel, record));
        readings.putAll(persistedChannel.getRegisterReadings(interval).stream()
                .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                        .identity())));
        return new ArrayList<>(readings.values());
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
        Map<Instant, BaseReadingRecord> readings = getCalculatedReadings(interval, BaseReadingRecord.class::cast);
        readings.putAll(persistedChannel.getReadings(interval).stream()
                .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                        .identity())));
        return new ArrayList<>(readings.values());
    }

    private <T extends BaseReadingRecord> Map<Instant, T> getCalculatedReadings(Range<Instant> interval, Function<BaseReadingRecord, T> mapper) {
        if(usagePoint.getEffectiveMetrologyConfigurations(interval).stream().anyMatch(emc -> emc.getMetrologyConfiguration().getContracts().contains(metrologyContract))) {
            return this.dataAggregationService.calculate(
                    usagePoint,
                    metrologyContract,
                    interval)
                    .getCalculatedDataFor(this.deliverable)
                    .stream()
                    .map(mapper::apply)
                    .collect(Collectors.toMap((Function<BaseReadingRecord, Instant>) BaseReadingRecord::getTimeStamp, Function
                            .identity()));
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval) {
        if (!getMainReadingType().equals(readingType)) {
            throw new IllegalArgumentException("Incorrect reading type. This channel supports only " + this.deliverable.getReadingType().getMRID());
        }
        return getRegisterReadings(interval);
    }

    @Override
    public Optional<BaseReadingRecord> getReading(Instant when) {
        return persistedChannel.getReadings(Range.openClosed(when.minusMillis(1L), when)).stream().findFirst();
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
    public ReadingQualityFetcher findReadingQualities() {
        return persistedChannel.findReadingQualities();
    }

    @Override
    public List<BaseReadingRecord> getReadingsUpdatedSince(ReadingType readingType, Range<Instant> interval, Instant since) {
        // TODO Data aggregation service always calculates readings on request, maybe it will be better to return the whole bunch of records (data export only).
        // return persistedChannel.getReadingsUpdatedSince(readingType, interval, since);
        return Collections.emptyList();
    }

    @Override
    public List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount) {
        return persistedChannel.getReadingsBefore(when, readingCount);
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount) {
        return persistedChannel.getReadingsOnOrBefore(when, readingCount);
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
                return calculatedFirstDateTime.compareTo(persistedChannelFirstDateTime) < 0 ? calculatedFirstDateTime : persistedChannelFirstDateTime;
            }
        }
        return persistedChannelFirstDateTime;
    }

    @Override
    public Instant getLastDateTime() {
        Instant persistedChannelLastDateTime = this.persistedChannel.getLastDateTime();
        CalculatedMetrologyContractData calculatedMetrologyContractData = this.dataAggregationService.calculate(this.usagePoint,
                this.metrologyContract, this.channelsContainer.getRange());
        if (!calculatedMetrologyContractData.isEmpty()) {
            List<? extends BaseReadingRecord> deliverableData = calculatedMetrologyContractData.getCalculatedDataFor(this.deliverable);
            if (!deliverableData.isEmpty()) {
                Instant calculatedLastDateTime = deliverableData.get(deliverableData.size() - 1).getTimeStamp();
                return persistedChannelLastDateTime == null || calculatedLastDateTime.compareTo(persistedChannelLastDateTime) >= 0 ? calculatedLastDateTime : persistedChannelLastDateTime;
            }
        }
        return persistedChannelLastDateTime;
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
    public MeterReading deleteReadings(Range<Instant> period) {
        return MeterReadingImpl.newInstance();
    }

    // ChannelContract methods ================================================

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

    @Override
    public List<IntervalReadingRecord> getPersistedIntervalReadings(Range<Instant> interval) {
        return persistedChannel.getIntervalReadings(interval);
    }

    @Override
    public List<IntervalReadingRecord> getCalculatedIntervalReadings(Range<Instant> interval) {
        return new ArrayList<>(getCalculatedReadings(interval, record -> new CalculatedReadingRecordImpl(this.persistedChannel, record))
                .values());
    }

    @Override
    public List<ReadingRecord> getPersistedRegisterReadings(Range<Instant> interval) {
        return persistedChannel.getRegisterReadings(interval);
    }

    @Override
    public List<ReadingRecord> getCalculatedRegisterReadings(Range<Instant> interval) {
        return new ArrayList<>(getCalculatedReadings(interval, record -> new CalculatedReadingRecordImpl(this.persistedChannel, record))
                .values());
    }

    private static class CalculatedReadingRecordImpl implements IntervalReadingRecord, ReadingRecord {

        private final BaseReadingRecord record;
        private final Channel channel;

        public CalculatedReadingRecordImpl(Channel channel, BaseReadingRecord record) {
            this.record = record;
            this.channel = channel;
        }

        @Override
        public List<Quantity> getQuantities() {
            return record.getQuantities();
        }

        @Override
        public Quantity getQuantity(int offset) {
            return record.getQuantity(offset);
        }

        @Override
        public Quantity getQuantity(ReadingType readingType) {
            return record.getQuantity(readingType);
        }

        @Override
        public ReadingType getReadingType() {
            return record.getReadingType();
        }

        @Override
        public ReadingType getReadingType(int offset) {
            return record.getReadingType(offset);
        }

        @Override
        public List<? extends ReadingType> getReadingTypes() {
            return record.getReadingTypes();
        }

        @Override
        public ProcessStatus getProcessStatus() {
            return record.getProcessStatus();
        }

        @Override
        public void setProcessingFlags(ProcessStatus.Flag... flags) {
            // do nothing
            // TODO store that reading and set processing flag for real entry?
        }

        @Override
        public CalculatedReadingRecordImpl filter(ReadingType readingType) {
            return this;
        }

        @Override
        public List<? extends ReadingQualityRecord> getReadingQualities() {
            return record.getReadingQualities();
        }

        @Override
        public BigDecimal getSensorAccuracy() {
            return record.getSensorAccuracy();
        }

        @Override
        public Instant getTimeStamp() {
            return record.getTimeStamp();
        }

        @Override
        public Instant getReportedDateTime() {
            return record.getReportedDateTime();
        }

        @Override
        public BigDecimal getValue() {
            return record.getValue();
        }

        @Override
        public String getSource() {
            return record.getSource();
        }

        @Override
        public Optional<Range<Instant>> getTimePeriod() {
            return record.getTimePeriod();
        }

        @Override
        public String getReason() {
            return null;
        }

        @Override
        public String getReadingTypeCode() {
            return channel.getMainReadingType().getMRID();
        }

        @Override
        public String getText() {
            return null;
        }
    }
}
