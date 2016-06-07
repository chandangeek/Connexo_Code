package com.elster.jupiter.metering.impl.config;

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
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MetrologyContractChannelsContainerImpl implements ChannelsContainer {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final DataAggregationService dataAggregationService;

    private long id;
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    private Reference<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = ValueReference.absent();
    private List<Channel> channels = new ArrayList<>();

    @Inject
    public MetrologyContractChannelsContainerImpl(ServerMetrologyConfigurationService metrologyConfigurationService, DataAggregationService dataAggregationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.dataAggregationService = dataAggregationService;
    }

    public MetrologyContractChannelsContainerImpl init(MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
        if (effectiveMetrologyConfiguration.getMetrologyConfiguration().getId() != metrologyContract.getMetrologyConfiguration().getId()) {
            throw new IllegalArgumentException("Metrology contract must be defined in the effective metrology configuration.");
        }
        this.metrologyContract.set(metrologyContract);
        this.effectiveMetrologyConfiguration.set(effectiveMetrologyConfiguration);
        return this;
    }

    @Override
    public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
        return null;
    }

    @Override
    public List<Channel> getChannels() {
        return Collections.unmodifiableList(this.channels);
    }

    @Override
    public Instant getStart() {
        return this.effectiveMetrologyConfiguration.get().getRange().lowerEndpoint();
    }

    @Override
    public Optional<Meter> getMeter() {
        return getMeter(this.metrologyConfigurationService.getClock().instant());
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return getUsagePoint(this.metrologyConfigurationService.getClock().instant());
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return Optional.empty();
    }

    @Override
    public Interval getInterval() {
        return this.effectiveMetrologyConfiguration.get().getInterval();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        if (!this.effectiveMetrologyConfiguration.get().getRange().isConnected(range)) {
            return Collections.emptySet();
        }
        return this.metrologyContract.get().getDeliverables()
                .stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        if (!this.effectiveMetrologyConfiguration.get().getRange().isConnected(range)) {
            return Collections.emptyList();
        }
        return null;
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        Range<Instant> channelsContainerRange = this.effectiveMetrologyConfiguration.get().getRange();
        if (!channelsContainerRange.isConnected(range)
                || channelsContainerRange.contains(since)) {
            return Collections.emptyList();
        }
        return null;
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return null;
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return null;
    }

    @Override
    public boolean hasData() {
        return false;
    }

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof MetrologyContractChannelsContainerImpl && ((MetrologyContractChannelsContainerImpl) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return this.effectiveMetrologyConfiguration.getOptional()
                .filter(emc -> emc.isEffectiveAt(instant))
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getUsagePoint);
    }

    @Override
    public ZoneId getZoneId() {
        return null;
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return null;
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex,
                                                          ReadingType readingType, Range<Instant> interval) {
        return null;
    }

    @Override
    public List<ChannelsContainer> getChannelsContainers() {
        return Collections.singletonList(this);
    }
}
