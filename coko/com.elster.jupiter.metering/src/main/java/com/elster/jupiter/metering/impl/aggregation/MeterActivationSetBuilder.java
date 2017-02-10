/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.metering.impl.ServerUsagePoint;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds {@link MeterActivationSet}s for a {@link UsagePoint} and a data aggregation period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (21:22)
 */
class MeterActivationSetBuilder {

    private final ServerUsagePoint usagePoint;
    private final Range<Instant> period;
    private final UsagePointMetrologyConfiguration metrologyConfiguration;
    private int sequenceNumber;

    MeterActivationSetBuilder(ServerUsagePoint usagePoint, Range<Instant> period) {
        this.usagePoint = usagePoint;
        this.period = period;
        this.metrologyConfiguration = this.usagePoint.getEffectiveMetrologyConfiguration(this.period.lowerEndpoint()).get().getMetrologyConfiguration();
    }

    MeterActivationSetBuilder(ServerUsagePoint usagePoint, Instant when) {
        this(usagePoint, Range.singleton(when));
    }

    List<MeterActivationSet> build() {
        this.sequenceNumber = 0;
        return Stream.concat(
                    this.getOverlappingMeterActivations().flatMap(this::switchTimestamps),
                    this.getOverlappingCalendarUsages().flatMap(this::switchTimestamps))
                .distinct()
                .sorted()
                .map(this::createMeterActivationSet)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    private Stream<MeterActivation> getOverlappingMeterActivations() {
        return this.usagePoint.getMeterActivations().stream().filter(each -> each.overlaps(this.period));
    }

    private Stream<Instant> switchTimestamps(MeterActivation meterActivation) {
        return this.switchTimestampsFromMeterActivationRange(meterActivation.getRange());
    }

    private Stream<ServerCalendarUsage> getOverlappingCalendarUsages() {
        return this.usagePoint.getTimeOfUseCalendarUsages().stream().filter(each -> each.overlaps(this.period));
    }

    private Stream<Instant> switchTimestamps(ServerCalendarUsage calendarUsage) {
        return this.switchTimestampsFromMeterActivationRange(calendarUsage.getRange());
    }

    private Stream<Instant> switchTimestampsFromMeterActivationRange(Range<Instant> meterActivationRange) {
        Stream.Builder<Instant> builder = Stream.builder();
        builder.add(meterActivationRange.lowerEndpoint());
        if (meterActivationRange.hasUpperBound() && this.period.contains(meterActivationRange.upperEndpoint())) {
            builder.add(meterActivationRange.upperEndpoint());
        }
        return builder.build();
    }

    private Optional<MeterActivationSet> createMeterActivationSet(Instant startDate) {
        List<MeterActivation> meterActivations =
                this.getOverlappingMeterActivations()
                        .filter(meterActivation -> meterActivation.getRange().contains(startDate))
                        .collect(Collectors.toList());
        Optional<Calendar> calendar = this.getOverlappingCalendarUsages().filter(calendarUsage -> calendarUsage.getRange().contains(startDate)).findAny().map(ServerCalendarUsage::getCalendar);
        if (meterActivations.isEmpty()) {
            return Optional.empty();
        } else {
            return this.createMeterActivationSet(startDate, meterActivations, calendar);
        }
    }

    private Optional<MeterActivationSet> createMeterActivationSet(Instant startDate, List<MeterActivation> meterActivations, Optional<Calendar> calendar) {
        this.sequenceNumber++;
        MeterActivationSetImpl set = new MeterActivationSetImpl(this.usagePoint, calendar.orElse(null), this.metrologyConfiguration, this.sequenceNumber, this.period, startDate);
        meterActivations.forEach(set::add);
        if (set.getRange().isEmpty()) {
            this.sequenceNumber--;
            return Optional.empty();
        } else {
            Loggers.ANALYSIS.debug(() -> new DataAggregationAnalysisLogger().meterActivationSetCreated(set));
            return Optional.of(set);
        }
    }

}