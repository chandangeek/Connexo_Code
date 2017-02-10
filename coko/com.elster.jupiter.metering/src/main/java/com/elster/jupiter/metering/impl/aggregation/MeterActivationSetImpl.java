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
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private final Calendar calendar;
    private final UsagePointMetrologyConfiguration configuration;
    private final List<MeterActivation> meterActivations = new ArrayList<>();
    private final int sequenceNumber;
    private final Range<Instant> period;
    private final Instant start;
    private Instant end;

    MeterActivationSetImpl(UsagePoint usagePoint, Calendar calendar, UsagePointMetrologyConfiguration configuration, int sequenceNumber, Range<Instant> period, Instant start) {
        this.usagePoint = usagePoint;
        this.calendar = calendar;
        this.configuration = configuration;
        this.sequenceNumber = sequenceNumber;
        this.period = period;
        this.start = start;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public int sequenceNumber() {
        return this.sequenceNumber;
    }

    @Override
    public Range<Instant> getRange() {
        return this.period.intersection(this.range());
    }

    private Range<Instant> range() {
        if (this.end != null) {
            return Range.closedOpen(this.start, this.end);
        } else {
            return Range.atLeast(this.start);
        }
    }

    @Override
    public void add(MeterActivation meterActivation) {
        this.meterActivations.add(meterActivation);
        Instant meterActivationEnd = meterActivation.getEnd();
        if (this.end == null || (meterActivationEnd != null && !meterActivationEnd.isAfter(this.end))) {
            this.end = meterActivationEnd;
        }
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

    public List<? extends ReadingQualityRecord> getReadingQualitiesFor(ReadingTypeRequirement requirement, Range range) {
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

}