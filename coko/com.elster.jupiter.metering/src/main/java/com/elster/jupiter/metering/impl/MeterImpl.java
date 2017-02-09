/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CannotDeleteMeter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyActive;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

class MeterImpl extends AbstractEndDeviceImpl<MeterImpl> implements Meter {

    private List<IMeterActivation> meterActivations = new ArrayList<>();
    private List<MeterConfigurationImpl> meterConfigurations = new ArrayList<>();

    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    MeterImpl(Clock clock, DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory,
              MeteringService meteringService, Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory, ServerMetrologyConfigurationService metrologyConfigurationService) {
        super(clock, dataModel, eventService, deviceEventFactory, MeterImpl.class);
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.deviceEventFactory = deviceEventFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public List<? extends MeterActivation> getMeterActivations() {
        return this.meterActivations
                .stream()
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends MeterActivation> getMeterActivations(Range<Instant> range) {
        return this.meterActivations
                .stream()
                .filter(activation -> activation.overlaps(range))
                .collect(Collectors.toList());
    }

    @Override
    public void store(QualityCodeSystem system, MeterReading meterReading) {
        new MeterReadingStorer(getDataModel(), meteringService, this, meterReading, thesaurus, getEventService(), deviceEventFactory).store(system);
    }

    @Override
    public MeterActivation activate(Instant from) {
        return this.activate(Range.atLeast(from));
    }

    @Override
    public MeterActivation activate(Range<Instant> range) {
        return this.activate(null, null, range);
    }

    @Override
    public MeterActivation activate(UsagePoint usagePoint, Instant from) {
        return this.activate(usagePoint, this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT), from);
    }

    @Override
    public MeterActivation activate(UsagePoint usagePoint, MeterRole meterRole, Instant from) {
        return this.activate(usagePoint, meterRole, Range.atLeast(from));
    }

    private MeterActivation activate(UsagePoint usagePoint, MeterRole meterRole, Range<Instant> range) {
        checkOverlaps(range);
        MeterActivationImpl result = meterActivationFactory.get().init(this, meterRole, usagePoint, range);
        result.save();
        meterActivations.add(result);
        return result;
    }

    private void checkOverlaps(Range<Instant> range) {
        if (meterActivations.stream()
                .filter(meterActivation -> meterActivation.getRange().isConnected(range))
                .anyMatch(meterActivation -> !meterActivation.getRange().intersection(range).isEmpty())) {
            throw new MeterAlreadyActive(thesaurus, this, range.lowerEndpoint());
        }
    }

    void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .reduce((m1, m2) -> m2)
                .ifPresent(last -> {
                    if (last.getRange().lowerEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                        throw new IllegalArgumentException("Invalid start date");
                    } else {
                        if (!last.getRange().hasUpperBound() || last.getRange().upperEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                            last.endAt(meterActivation.getRange().lowerEndpoint());
                        }
                    }
                });
        Optional<IMeterActivation> first = meterActivations.stream().filter(activation -> activation.getId() != meterActivation.getId()).findFirst();
        if (!first.isPresent()) {
            meterActivations.add(meterActivation);
        }
    }


    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return meterActivations.stream()
                .map(MeterActivation.class::cast)
                .filter(MeterActivation::isCurrent)
                .findAny();
    }

    @Override
    public Optional<? extends MeterActivation> getMeterActivation(Instant when) {
        return meterActivations.stream()
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .findFirst();
    }


    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return MeterActivationsImpl.from(meterActivations, range).getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when, readingType, count);
    }


    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return MeterActivationsImpl.from(meterActivations).hasData();
    }

    @Override
    public void delete() {
        if (meterActivations.size() > 0) {
            throw new CannotDeleteMeter(thesaurus, MessageSeeds.CANNOT_DELETE_METER_METER_ACTIVATIONS_EXIST, getName());
        }
        super.delete();
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities(Range<Instant> range) {
        if (!range.hasLowerBound() && !range.hasUpperBound()) {
            throw new IllegalArgumentException();
        }
        QueryExecutor<ReadingQualityRecord> query = getDataModel().query(ReadingQualityRecord.class, Channel.class, ChannelsContainer.class, MeterActivation.class);
        Condition condition = Where.where("channel.channelsContainer.meterActivation.meter").isEqualTo(this);
        condition = condition.and(Where.where("readingTimestamp").in(range));
        return query.select(condition);
    }

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof Meter && ((Meter) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return Optional.of(this);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return getMeterActivation(instant).flatMap(MeterActivation::getUsagePoint);
    }

    @Override
    public ZoneId getZoneId() {
        return getCurrentMeterActivation()
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getZoneId)
                .orElse(ZoneId.systemDefault());
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return getCurrentMeterActivation()
                .map(MeterActivation::getChannelsContainer)
                .map(channelsContainer -> channelsContainer.toList(readingType, exportInterval))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex,
                                                          ReadingType readingType, Range<Instant> interval) {
        return meterActivations.stream()
                .map(MeterActivation::getChannelsContainer)
                .flatMap(channelsContainer -> channelsContainer.getReadingQualities(qualityCodeSystems, qualityCodeIndex, readingType, interval).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelsContainer> getChannelsContainers() {
        return getMeterActivations().stream().map(MeterActivation::getChannelsContainer).collect(Collectors.toList());
    }

    void addConfiguration(MeterConfigurationImpl meterConfiguration) {
        meterConfigurations.add(meterConfiguration);
    }

    @Override
    public MeterConfigurationBuilder startingConfigurationOn(Instant startTime) {
        return new MeterConfigurationBuilderImpl(getDataModel(), this, startTime);
    }

    @Override
    public Optional<MeterConfiguration> getConfiguration(Instant time) {
        return meterConfigurations.stream()
                .filter(meterConfiguration -> meterConfiguration.isEffectiveAt(time))
                .map(MeterConfiguration.class::cast)
                .findAny();
    }

    void refreshMeterActivations() {
        this.meterActivations.clear();
        this.meterActivations.addAll(getDataModel().query(MeterActivationImpl.class)
                .select(where("meter").isEqualTo(this)));
    }
}
