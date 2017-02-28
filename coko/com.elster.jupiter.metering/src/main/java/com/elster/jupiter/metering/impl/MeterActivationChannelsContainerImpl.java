/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MeterActivationChannelsContainerImpl extends ChannelsContainerImpl {
    private final DataAggregationService aggregationService;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();

    @Inject
    MeterActivationChannelsContainerImpl(ServerMeteringService meteringService,
                                         EventService eventService,
                                         DataAggregationService aggregationService,
                                         Provider<ChannelBuilder> channelBuilder) {
        super(meteringService, eventService, channelBuilder);
        this.aggregationService = aggregationService;
    }

    public MeterActivationChannelsContainerImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        return this;
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return this.meterActivation.get().getMultiplier(type);
    }

    @Override
    public Optional<Meter> getMeter() {
        return meterActivation.flatMap(MeterActivation::getMeter);
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        MeterActivation meterActivation = this.meterActivation.get();
        return meterActivation.isEffectiveAt(instant) ? meterActivation.getMeter() : Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        MeterActivation meterActivation = this.meterActivation.get();
        return meterActivation.isEffectiveAt(instant) ? meterActivation.getUsagePoint() : Optional.empty();
    }

    @Override
    public Interval getInterval() {
        return this.meterActivation.get().getInterval();
    }

    @Override
    public Map<Channel, Range<Instant>> findDependentChannelScope(Map<Channel, Range<Instant>> scope) {
        return scope.values().stream()
                .reduce(Range::span)
                .flatMap(overallRange -> getMeter().map(meter -> meter.getMeterActivations(overallRange).stream()
                        .flatMap(meterActivation -> meterActivation.getUsagePoint()
                                .map(usagePoint -> usagePoint.getEffectiveMetrologyConfigurations(meterActivation.getRange()))
                                .map(List::stream)
                                .orElse(Stream.empty()))
                        .filter(effectiveMC -> !effectiveMC.getRange().isEmpty())
                        .flatMap(effectiveMC -> effectiveMC.getMetrologyConfiguration().getContracts().stream()
                                .flatMap(contract -> effectiveMC.getChannelsContainer(contract)
                                        .map(container -> {
                                            MetrologyContractCalculationIntrospector introspector =
                                                    aggregationService.introspect(effectiveMC.getUsagePoint(), contract, effectiveMC.getRange());
                                            Map<Channel, Range<Instant>> directlyDependentScope = contract.getDeliverables().stream()
                                                    .map(deliverable -> Pair.of(
                                                            deliverable.getReadingType(),
                                                            filterDependencyScope(scope, deliverable, introspector)))
                                                    .filter(Pair::hasLast)
                                                    .map(readingTypeAndRange -> Pair.of(
                                                            container.getChannel(readingTypeAndRange.getFirst()).orElse(null),
                                                            readingTypeAndRange.getLast()))
                                                    .filter(Pair::hasFirst)
                                                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
                                            Map<Channel, Range<Instant>> transitiveDependentScope = container
                                                    .findDependentChannelScope(directlyDependentScope);
                                            return Stream.concat(directlyDependentScope.entrySet().stream(),
                                                    transitiveDependentScope.entrySet().stream());
                                        })
                                        .orElse(Stream.empty())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Range::span))))
                .orElse(Collections.emptyMap());
    }

    private static Range<Instant> filterDependencyScope(Map<Channel, Range<Instant>> scope,
                                                        ReadingTypeDeliverable deliverable,
                                                        MetrologyContractCalculationIntrospector introspector) {
        return introspector.getChannelUsagesFor(deliverable).stream()
                .map(channelUsage -> intersectWithScope(channelUsage, scope))
                .flatMap(Functions.asStream())
                .reduce(Range::span)
                .orElse(null);
    }

    private static Optional<Range<Instant>> intersectWithScope(MetrologyContractCalculationIntrospector.ChannelUsage channelUsage,
                                                               Map<Channel, Range<Instant>> scope) {
        Range<Instant> scopeRange = scope.get(channelUsage.getChannel());
        return Optional.ofNullable(scopeRange)
                .filter(channelUsage.getRange()::isConnected)
                .map(channelUsage.getRange()::intersection)
                .filter(range -> !range.isEmpty());
    }
}
