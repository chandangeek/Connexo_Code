package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyContractChannelsContainer;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MetrologyContractChannelsContainerWrapper implements MetrologyContractChannelsContainer {
    private final ChannelsContainer channelsContainer;
    private final MetrologyContract metrologyContract;

    private MetrologyContractChannelsContainerWrapper(ChannelsContainer channelsContainer, MetrologyContract metrologyContract) {
        this.channelsContainer = channelsContainer;
        this.metrologyContract = metrologyContract;
    }

    public static MetrologyContractChannelsContainer from(ChannelsContainer channelsContainer, MetrologyContract metrologyContract) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            return (MetrologyContractChannelsContainer) channelsContainer;
        }
        return new MetrologyContractChannelsContainerWrapper(channelsContainer, metrologyContract);
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract;
    }

    @Override
    public long getId() {
        return channelsContainer.getId();
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return channelsContainer.getMultiplier(type);
    }

    @Override
    public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
        return channelsContainer.createChannel(main, readingTypes);
    }

    @Override
    public List<Channel> getChannels() {
        return channelsContainer.getChannels();
    }

    @Override
    public Optional<Channel> getChannel(ReadingType readingType) {
        return channelsContainer.getChannel(readingType);
    }

    @Override
    public Instant getStart() {
        return channelsContainer.getStart();
    }

    @Override
    public Optional<Meter> getMeter() {
        return channelsContainer.getMeter();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return channelsContainer.getUsagePoint();
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return channelsContainer.getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return channelsContainer.getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return channelsContainer.getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return channelsContainer.getReadingsBefore(when, readingType, count);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return channelsContainer.getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return channelsContainer.hasData();
    }

    @Override
    public boolean is(ReadingContainer other) {
        return channelsContainer.is(other);
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return channelsContainer.getMeter(instant);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return channelsContainer.getUsagePoint(instant);
    }

    @Override
    public ZoneId getZoneId() {
        return channelsContainer.getZoneId();
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return channelsContainer.toList(readingType, exportInterval);
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex, ReadingType readingType, Range<Instant> interval) {
        return channelsContainer.getReadingQualities(qualityCodeSystems, qualityCodeIndex, readingType, interval);
    }

    @Override
    public List<ChannelsContainer> getChannelsContainers() {
        return Collections.singletonList(this);
    }

    @Override
    public Interval getInterval() {
        return channelsContainer.getInterval();
    }
}
