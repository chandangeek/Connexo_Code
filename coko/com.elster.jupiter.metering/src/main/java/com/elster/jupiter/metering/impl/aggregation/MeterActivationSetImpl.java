/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link MeterActivationSet} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (13:18)
 */
class MeterActivationSetImpl implements MeterActivationSet {
    private final UsagePoint usagePoint;
    private final List<MeterActivation> meterActivations = new ArrayList<>();
    private Calendar calendar;
    private final Map<String, SyntheticLoadProfile> syntheticLoadProfiles = new HashMap<>();
    private final UsagePointMetrologyConfiguration configuration;
    private final int sequenceNumber;
    private Range<Instant> period;

    MeterActivationSetImpl(UsagePoint usagePoint, UsagePointMetrologyConfiguration configuration, int sequenceNumber, Range<Instant> period, Instant start) {
        this.usagePoint = usagePoint;
        this.configuration = configuration;
        this.sequenceNumber = sequenceNumber;
        this.period = period.intersection(Range.atLeast(start));
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    @Override
    public int sequenceNumber() {
        return this.sequenceNumber;
    }

    @Override
    public Range<Instant> getRange() {
        return this.period;
    }

    @Override
    public void avoidOverlapWith(MeterActivationSet other) {
        boolean overlaps = !ImmutableRangeSet.of(this.getRange()).subRangeSet(other.getRange()).isEmpty();
        if (overlaps) {
            this.period = Range.closedOpen(this.period.lowerEndpoint(), other.getRange().lowerEndpoint());
        }
    }

    @Override
    public void add(MeterActivation meterActivation) {
        this.meterActivations.add(meterActivation);
        this.period = this.period.intersection(meterActivation.getRange());
    }

    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public void setCalendar(ServerCalendarUsage calendarUsage) {
        this.calendar = calendarUsage.getCalendar();
        this.period = this.period.intersection(calendarUsage.getRange());
    }

    @Override
    public SyntheticLoadProfile getSyntheticLoadProfile(String propertySpecName) {
        return this.syntheticLoadProfiles.get(propertySpecName);
    }

    @Override
    public void addSyntheticLoadProfile(SyntheticLoadProfileUsage syntheticLoadProfileUsage) {
        syntheticLoadProfileUsage
                .getSyntheticLoadProfilePropertyNames()
                .stream()
                .forEach(propertySpecName -> this.syntheticLoadProfiles.put(propertySpecName, syntheticLoadProfileUsage.getSyntheticLoadProfile(propertySpecName)));
        this.period = this.period.intersection(syntheticLoadProfileUsage.getRange());
    }

    @Override
    public List<MeterActivation> getMeterActivations() {
        return Collections.unmodifiableList(this.meterActivations);
    }

    @Override
    public List<Channel> getChannels() {
        return this.meterActivations
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getChannels)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Channel> getMatchingChannelsFor(ReadingTypeRequirement requirement) {
        Stream<MeterActivation> meterActivations = this.getMeterActivationsFor(requirement);
        return meterActivations
                .map(MeterActivation::getChannelsContainer)
                .map(requirement::getMatchingChannelsFor)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<? extends ReadingQualityRecord> getReadingQualitiesFor(ReadingTypeRequirement requirement, Range<Instant> range) {
        Optional<MeterRole> meterRole = configuration.getMeterRoleFor(requirement);
        if (meterRole.isPresent()) {
            Optional<MeterActivation> meterActivation =
                    meterActivations.stream().filter(ma -> ma.getMeterRole().isPresent() && ma.getMeterRole().get().equals(meterRole.get())).findAny();
            if (meterActivation.isPresent()) {
                return meterActivation.get().getMeter().get().getReadingQualities(range);
            }
        }
        return new ArrayList<>();
    }

    public boolean contains(Instant instant) {
        return this.meterActivations.stream().filter(ma -> ma.getRange().contains(instant)).count() == this.meterActivations.size();
    }

    private Stream<MeterActivation> getMeterActivationsFor(ReadingTypeRequirement requirement) {
        Optional<MeterRole> meterRole = this.configuration.getMeterRoleFor(requirement);
        Stream<MeterActivation> meterActivations;
        if (meterRole.isPresent()) {
            meterActivations = this.meterActivations
                    .stream()
                    .filter(meterActivation -> meterActivation.getMeterRole().isPresent())
                    .filter(meterActivation -> meterActivation.getMeterRole().get().equals(meterRole.get()));
        } else {
            meterActivations = this.meterActivations.stream();
        }
        return meterActivations;
    }

    @Override
    public Optional<BigDecimal> getMultiplier(ReadingTypeRequirement requirement, MultiplierType type) {
        return this.getMeterActivationsFor(requirement)
                .map(meterActivation -> meterActivation.getMultiplier(type))
                .flatMap(Functions.asStream())
                .findFirst();
    }

    public ZoneId getZoneId() {
        return meterActivations.iterator().next().getZoneId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("period", this.period)
                .toString();
    }

}