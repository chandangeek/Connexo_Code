/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toList;

@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_DATA_LOGGER_FOR_ITSELF + "}")
@OriginDeviceTypeIsDataLogger(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NOT_A_DATALOGGER_SLAVE_DEVICE + "}")
@GatewayDeviceTypeIsDataLoggerEnabled(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.GATEWAY_NOT_DATALOGGER_ENABLED + "}")
@AllSlaveChannelsIncluded(groups = {Save.Create.class}, message = "{" + DataLoggerLinkException.NO_MAPPING_FOR_ALL_SLAVE_CHANNELS + "}")
@AllDataLoggerChannelsAvailable(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.DATA_LOGGER_CHANNEL_ALREADY_REFERENCED + "}")
public class DataLoggerReferenceImpl extends AbstractPhysicalGatewayReferenceImpl implements DataLoggerReference {

    private final Thesaurus thesaurus;
    private List<DataLoggerChannelUsage> dataLoggerChannelUsages = new ArrayList<>();

    @Inject
    public DataLoggerReferenceImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DataLoggerReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

    @Override
    public boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel) {
        return dataLoggerChannelUsages.add(new DataLoggerChannelUsageImpl().createFor(this, slaveChannel, dataLoggerChannel));
    }

    public List<DataLoggerChannelUsage> getDataLoggerChannelUsages() {
        return Collections.unmodifiableList(dataLoggerChannelUsages);
    }

    @Override
    public boolean isTerminated() {
        return getRange().hasUpperBound();
    }

    /**
     * Data from each DataLogger Channel is transferred to the slave channel for this DataLoggerReference's interval
     */
    void transferChannelDataToSlave(TopologyServiceImpl topologyService) {
        this.dataLoggerChannelUsages.stream().forEach((channelUsage) -> transferChannelDataToSlave(topologyService, channelUsage));
    }

    /**
     * Closes the current interval.
     */
    public void terminate(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw DataLoggerLinkException.invalidTerminationDate(thesaurus);
        }

        // Data on the slave channels having a dat
        this.dataLoggerChannelUsages.stream().forEach((usage) -> transferChannelDataToDataLogger(usage, closingDate));
        super.terminate(closingDate);
    }

    private void transferChannelDataToSlave(TopologyServiceImpl topologyService, DataLoggerChannelUsage channelUsage) {
        Channel dataloggerChannel = channelUsage.getDataLoggerChannel();
        Channel slaveChannel = channelUsage.getSlaveChannel();

        if (dataloggerChannel.hasData()) {
            ReadingType dataLoggerCollectedReadingType;
            ReadingType slaveCollectedReadingType;
            Optional<? extends ReadingType> bulkQuantityReadingType = dataloggerChannel.getBulkQuantityReadingType();
            if (bulkQuantityReadingType.isPresent()) {
                dataLoggerCollectedReadingType = bulkQuantityReadingType.get();
            } else {
                dataLoggerCollectedReadingType = dataloggerChannel.getMainReadingType();
            }
            Optional<? extends ReadingType> slaveBulkQuantityReadingType = slaveChannel.getBulkQuantityReadingType();
            if (slaveBulkQuantityReadingType.isPresent()) {
                slaveCollectedReadingType = slaveBulkQuantityReadingType.get();
            } else {
                slaveCollectedReadingType = slaveChannel.getMainReadingType();
            }
            FilteredMeterReading meterReading = new FilteredMeterReading(dataloggerChannel.deleteReadings(getInterval().toOpenClosedRange()),
                    block -> block.getReadingTypeCode().equals(dataLoggerCollectedReadingType.getMRID()),
                    reading -> reading.getReadingTypeCode().equals(dataLoggerCollectedReadingType.getMRID()),
                    // do copy reading qualities of ENDDEVICE system AND reading quality indicating of EDITED category
                    rq -> (rq.getType().system().map(QualityCodeSystem.ENDDEVICE::equals).orElse(false) || rq.getType().category().map(QualityCodeCategory.EDITED::equals).orElse(false)),
                    ImmutableMap.of(dataLoggerCollectedReadingType, slaveCollectedReadingType)
            );
            if (!meterReading.isEmpty()) {
                this.getOrigin().store(meterReading);
                if (dataloggerChannel.isRegular()) {
                    updateLastReadingIfApplicable(topologyService, slaveChannel);
                }
            }
        }
    }

    private void updateLastReadingIfApplicable(TopologyServiceImpl topologyService, Channel channel) {
        LoadProfile toUpdate = topologyService.getChannel(this.getOrigin(), channel).map(com.energyict.mdc.device.data.Channel::getLoadProfile).get();
        if (!toUpdate.getLastReading().isPresent() || toUpdate.getLastReading().get().isBefore(channel.getLastDateTime())) {
            this.getOrigin().getLoadProfileUpdaterFor(toUpdate).setLastReading(channel.getLastDateTime()).update();
        }
    }

    private void transferChannelDataToDataLogger(DataLoggerChannelUsage channelUsage, Instant start) {
        // Make sure we are using the current Meter Activation channels instances
        Channel slaveChannel = channelUsage.getSlaveChannel();
        Channel dataloggerChannel = channelUsage.getDataLoggerChannel();
        if (slaveChannel.hasData()) {
            ReadingType dataLoggerCollectedReadingType;
            ReadingType slaveCollectedReadingType;

            Optional<? extends ReadingType> bulkQuantityReadingType = dataloggerChannel.getBulkQuantityReadingType();
            if (bulkQuantityReadingType.isPresent()) {
                dataLoggerCollectedReadingType = bulkQuantityReadingType.get();
            } else {
                dataLoggerCollectedReadingType = dataloggerChannel.getMainReadingType();
            }
            Optional<? extends ReadingType> slaveBulkQuantityReadingType = slaveChannel.getBulkQuantityReadingType();
            if (slaveBulkQuantityReadingType.isPresent()) {
                slaveCollectedReadingType = slaveBulkQuantityReadingType.get();
            } else {
                slaveCollectedReadingType = slaveChannel.getMainReadingType();
            }
            FilteredMeterReading meterReading = new FilteredMeterReading(slaveChannel.deleteReadings(Range.atLeast(start)),
                    block -> block.getReadingTypeCode().equals(slaveCollectedReadingType.getMRID()),
                    reading -> reading.getReadingTypeCode().equals(slaveCollectedReadingType.getMRID()),
                    rq -> rq.getType().system().map(QualityCodeSystem.ENDDEVICE::equals).orElse(false),
                    ImmutableMap.of(slaveCollectedReadingType, dataLoggerCollectedReadingType)
            );
            if (!meterReading.isEmpty()) {
                List<BaseReading> readings;
                if (dataLoggerCollectedReadingType.isRegular()) {
                    readings = meterReading.getIntervalBlocks()
                            .stream()
                            .filter(ib -> dataLoggerCollectedReadingType.getMRID().equals(ib.getReadingTypeCode()))
                            .flatMap(ib -> ib.getIntervals().stream())
                            .collect(Collectors.toList());
                } else {
                    readings = meterReading.getReadings().stream().filter(reading -> dataLoggerCollectedReadingType.getMRID().equals(reading.getReadingTypeCode())).collect(Collectors.toList());
                }
                dataloggerChannel.getCimChannel(dataLoggerCollectedReadingType).orElseThrow(IllegalArgumentException::new).editReadings(QualityCodeSystem.MDC, readings);
            }
        }
    }

    private class FilteredMeterReading implements MeterReading {

        private final MeterReading decorated;
        private final Predicate<IntervalBlock> intervalBlockFilter;
        private final Predicate<Reading> readingFilter;
        private final Predicate<ReadingQuality> readingQualityPredicate;
        private final Map<String, String> readingTypeMap;

        public FilteredMeterReading(MeterReading decorated, Predicate<IntervalBlock> intervalBlockFilter, Predicate<Reading> readingFilter, Predicate<ReadingQuality> readingQualityPredicate, Map<ReadingType, ReadingType> readingTypeReplacement) {
            this.decorated = decorated;
            this.intervalBlockFilter = intervalBlockFilter;
            this.readingFilter = readingFilter;
            this.readingQualityPredicate = readingQualityPredicate;
            this.readingTypeMap = new HashMap<>();
            readingTypeReplacement.entrySet().forEach(entry -> readingTypeMap.put(entry.getKey().getMRID(), entry.getValue().getMRID()));
        }

        public boolean isEmpty() {
            return getReadings().isEmpty() &&
                    getEvents().isEmpty() &&
                    noIntervals();
        }

        private boolean noIntervals() {
            return !getIntervalBlocks().stream().filter(not(block -> block.getIntervals().isEmpty())).findAny().isPresent();
        }

        @Override
        public List<Reading> getReadings() {
            return decorated.getReadings().stream()
                    .filter(readingFilter)
                    .map(reading -> new FilteredReading(reading, readingQualityPredicate, readingTypeMap.get(reading.getReadingTypeCode())))
                    .map(Reading.class::cast)
                    .collect(toList());
        }

        @Override
        public List<IntervalBlock> getIntervalBlocks() {
            return decorated.getIntervalBlocks()
                    .stream()
                    .filter(intervalBlockFilter)
                    .map(block -> new FilteredIntervalBlock(block, readingQualityPredicate, readingTypeMap.get(block.getReadingTypeCode())))
                    .map(IntervalBlock.class::cast)
                    .collect(toList());
        }

        @Override
        public List<EndDeviceEvent> getEvents() {
            return decorated.getEvents();
        }
    }

    private class FilteredIntervalBlock implements IntervalBlock {

        private final IntervalBlock decorated;
        private final Predicate<ReadingQuality> readingQualityPredicate;
        private final String readingTypeReplacement;

        public FilteredIntervalBlock(IntervalBlock decorated, Predicate<ReadingQuality> readingQualityPredicate, String readingTypeReplacement) {
            this.decorated = decorated;
            this.readingQualityPredicate = readingQualityPredicate;
            this.readingTypeReplacement = readingTypeReplacement;
        }

        @Override
        public List<IntervalReading> getIntervals() {
            return decorated.getIntervals().stream().map(ir -> new FilteredIntervalReading(ir, readingQualityPredicate)).map(IntervalReading.class::cast).collect(toList());
        }

        @Override
        public String getReadingTypeCode() {
            return readingTypeReplacement != null ? readingTypeReplacement : decorated.getReadingTypeCode();
        }
    }

    private class FilteredIntervalReading implements IntervalReading {
        private final IntervalReading decorated;
        private final Predicate<ReadingQuality> readingQualityFilter;

        public FilteredIntervalReading(IntervalReading decorated, Predicate<ReadingQuality> readingQualityFilter) {
            this.decorated = decorated;
            this.readingQualityFilter = readingQualityFilter;
        }

        @Override
        public BigDecimal getSensorAccuracy() {
            return decorated.getSensorAccuracy();
        }

        @Override
        public Instant getTimeStamp() {
            return decorated.getTimeStamp();
        }

        @Override
        public Instant getReportedDateTime() {
            return decorated.getReportedDateTime();
        }

        @Override
        public BigDecimal getValue() {
            return decorated.getValue();
        }

        @Override
        public String getSource() {
            return decorated.getSource();
        }

        @Override
        public Optional<Range<Instant>> getTimePeriod() {
            return decorated.getTimePeriod();
        }

        @Override
        public List<? extends ReadingQuality> getReadingQualities() {
            return decorated.getReadingQualities().stream().filter(readingQualityFilter::test).collect(Collectors.toList());
        }
    }

    private class FilteredReading implements Reading {

        private final Reading decoratedReading;
        private final Predicate<ReadingQuality> readingQualityFilter;
        private final String readingType;

        public FilteredReading(Reading decoratedReading, Predicate<ReadingQuality> readingQualityFilter, String replacementReadingType) {
            this.decoratedReading = decoratedReading;
            this.readingQualityFilter = readingQualityFilter;
            this.readingType = replacementReadingType;
        }

        @Override
        public String getReason() {
            return decoratedReading.getReason();
        }

        @Override
        public String getReadingTypeCode() {
            return readingType != null ? readingType : decoratedReading.getReadingTypeCode();
        }

        @Override
        public String getText() {
            return decoratedReading.getText();
        }

        @Override
        public BigDecimal getSensorAccuracy() {
            return decoratedReading.getSensorAccuracy();
        }

        @Override
        public Instant getTimeStamp() {
            return decoratedReading.getTimeStamp();
        }

        @Override
        public Instant getReportedDateTime() {
            return decoratedReading.getReportedDateTime();
        }

        @Override
        public BigDecimal getValue() {
            return decoratedReading.getValue();
        }

        @Override
        public String getSource() {
            return decoratedReading.getSource();
        }

        @Override
        public Optional<Range<Instant>> getTimePeriod() {
            return decoratedReading.getTimePeriod();
        }

        @Override
        public List<? extends ReadingQuality> getReadingQualities() {
            return decoratedReading.getReadingQualities().stream().filter(readingQualityFilter::test).collect(Collectors.toList());
        }
    }

}
