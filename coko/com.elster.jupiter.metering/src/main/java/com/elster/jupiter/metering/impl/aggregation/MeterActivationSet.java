/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Models a set of {@link MeterActivation}s that are all active at the same time
 * but with a different {@link com.elster.jupiter.metering.config.MeterRole}.
 * A MeterActivationSet is capable of finding the matching {@link Channel}s
 * for a {@link ReadingTypeRequirement} by looking at the MeterRole that
 * will provide the data for the ReadingTypeRequirement.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (13:01)
 */
public interface MeterActivationSet {

    UsagePoint getUsagePoint();

    int sequenceNumber();

    Range<Instant> getRange();

    void avoidOverlapWith(MeterActivationSet other);

    List<MeterActivation> getMeterActivations();

    void add(MeterActivation meterActivation);

    Calendar getCalendar();

    void setCalendar(ServerCalendarUsage calendarUsage);

    SyntheticLoadProfile getSyntheticLoadProfile(String propertySpecName);

    void addSyntheticLoadProfile(SyntheticLoadProfileUsage syntheticLoadProfileUsage);

    /**
     * Return the complete List of {@link Channel}s that are available
     * in all of the {@link MeterActivation}s in this set.
     *
     * @return The List of Channel
     */
    List<Channel> getChannels();

    /**
     * Return the List of {@link Channel}s for the specified {@link ReadingTypeRequirement}
     * by looking at the {@link com.elster.jupiter.metering.config.MeterRole} that
     * is configured to provide the data for the ReadingTypeRequirement.
     *
     * @param requirement The ReadingTypeRequirement
     * @return The List of matching Channel
     */
    List<Channel> getMatchingChannelsFor(ReadingTypeRequirement requirement);

    Optional<BigDecimal> getMultiplier(ReadingTypeRequirement requirement, MultiplierType type);

    boolean contains(Instant instant);

    ZoneId getZoneId();

    List<? extends ReadingQualityRecord> getReadingQualitiesFor(ReadingTypeRequirement requirement, Range<Instant> range);

}