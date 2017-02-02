/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.ChannelContract;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link MetrologyContractCalculationIntrospector} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-01 (14:44)
 */
class MetrologyContractCalculationIntrospectorImpl implements MetrologyContractCalculationIntrospector {
    private final UsagePoint usagePoint;
    private final MetrologyContract contract;
    private final List<ReadingTypeDeliverableForMeterActivationSet> deliverablesPerMeterActivation;

    MetrologyContractCalculationIntrospectorImpl(UsagePoint usagePoint, MetrologyContract contract, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        this.usagePoint = usagePoint;
        this.contract = contract;
        this.deliverablesPerMeterActivation = deliverablesPerMeterActivation.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return this.contract;
    }

    @Override
    public List<ChannelUsage> getChannelUsagesFor(ReadingTypeDeliverable deliverable) {
        return this.deliverablesPerMeterActivation
                .stream()
                .filter(each -> each.getDeliverable().equals(deliverable))
                .map(this::toChannelUsages)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<CalendarUsage> getCalendarUsagesFor(ReadingTypeDeliverable deliverable) {
        return Collections.emptyList();
    }

    private Collection<ChannelUsage> toChannelUsages(ReadingTypeDeliverableForMeterActivationSet readingTypeDeliverableForMeterActivationSet) {
        return readingTypeDeliverableForMeterActivationSet
                .getPreferredChannels()
                .stream()
                .map(channel -> new ChannelUsageImpl(channel, readingTypeDeliverableForMeterActivationSet.getRange()))
                .collect(Collectors.toList());
    }

    private static class ChannelUsageImpl implements ChannelUsage {
        private final ChannelContract channel;
        private final Range<Instant> range;

        private ChannelUsageImpl(ChannelContract channel, Range<Instant> range) {
            this.channel = channel;
            this.range = range;
        }

        @Override
        public Channel getChannel() {
            return channel;
        }

        @Override
        public Range<Instant> getRange() {
            return range;
        }
    }

}