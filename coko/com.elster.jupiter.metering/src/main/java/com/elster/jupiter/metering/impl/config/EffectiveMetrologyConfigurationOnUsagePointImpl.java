/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Provides an implementation for the {@link EffectiveMetrologyConfigurationOnUsagePoint} interface.
 */
public class EffectiveMetrologyConfigurationOnUsagePointImpl implements EffectiveMetrologyConfigurationOnUsagePoint {

    private final DataModel dataModel;
    private final EventService eventService;

    @SuppressWarnings("unused")//Managed by ORM
    private Interval interval;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<UsagePointMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    private List<EffectiveMetrologyContractOnUsagePoint> effectiveContracts = new ArrayList<>();

    private long id;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public EffectiveMetrologyConfigurationOnUsagePointImpl(DataModel dataModel, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    public EffectiveMetrologyConfigurationOnUsagePointImpl initAndSave(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = Interval.startAt(start);
        return this;
    }

    public EffectiveMetrologyConfigurationOnUsagePointImpl initAndSaveWithInterval(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration, Interval interval) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = interval;
        return this;
    }

    @Override
    public UsagePointMetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration.get();
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint.get();
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
        if (getRange().isEmpty()) {
            Set<ChannelsContainer> channelsContainers = effectiveContracts.stream()
                    .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                    .collect(Collectors.toSet());
            effectiveContracts.clear();
            eventService.postEvent(EventType.CHANNELS_CONTAINERS_CLIPPED.topic(), new EventType.ChannelsContainersClippedEvent(channelsContainers));
        } else {
            clip(effectiveContracts, closingDate);
        }
        this.dataModel.update(this);
    }

    @Override
    public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract) {
        return effectiveContracts.stream()
                .filter(effectiveContract -> effectiveContract.getMetrologyContract().equals(metrologyContract))
                .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                .findAny();
    }

    @Override
    public Optional<AggregatedChannel> getAggregatedChannel(MetrologyContract metrologyContract, ReadingType readingType) {
        return effectiveContracts.stream()
                .filter(effectiveContract -> effectiveContract.getMetrologyContract().equals(metrologyContract))
                .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                .map(channelsContainer -> channelsContainer.getChannel(readingType))
                .filter(channel -> channel.isPresent() && channel.get() instanceof AggregatedChannel)
                .map(Optional::get)
                .map(AggregatedChannel.class::cast)
                .findAny();
    }

    @Override
    public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract, Instant when) {
        return effectiveContracts.stream()
                .filter(effectiveContract -> effectiveContract.getMetrologyContract()
                        .equals(metrologyContract) && effectiveContract.isEffectiveAt(when))
                .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                .findAny();
    }

    public void createEffectiveMetrologyContracts() {
        this.createEffectiveMetrologyContracts(Collections.emptySet());
    }

    public void createEffectiveMetrologyContracts(Set<MetrologyContract> optionalContractsToCreate) {
        getMetrologyConfiguration().getContracts().stream()
                .filter(metrologyContract -> metrologyContract.isMandatory() || optionalContractsToCreate.contains(metrologyContract))
                .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                .map(metrologyContract -> dataModel.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class).init(this, metrologyContract))
                .forEach(this.effectiveContracts::add);
    }

    @Override
    public void activateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {
        this.effectiveContracts.add(this.dataModel.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class)
                .init(this, metrologyContract, Range.atLeast(when)));
    }

    @Override
    public void deactivateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {
        this.effectiveContracts.stream()
                .filter(effectiveMetrologyContract -> !effectiveMetrologyContract.getMetrologyContract().isMandatory())
                .filter(effectiveMetrologyContract -> effectiveMetrologyContract.getMetrologyContract().equals(metrologyContract))
                .findFirst()
                .ifPresent(effectiveMetrologyContract -> clip(Collections.singletonList(effectiveMetrologyContract), when));
    }

    private void clip(Collection<EffectiveMetrologyContractOnUsagePoint> metrologyContracts, Instant closingTimestamp) {
        List<EffectiveMetrologyContractOnUsagePoint> contractsToRemove = new ArrayList<>(metrologyContracts.size());
        Set<ChannelsContainer> affectedContainers = new HashSet<>(metrologyContracts.size(), 1);
        metrologyContracts.forEach(contract -> {
            if (Ranges.does(contract.getRange()).startAfter(closingTimestamp.minusMillis(1))) {
                contractsToRemove.add(contract);
                affectedContainers.add(contract.getChannelsContainer());
            } else if (contract.isEffectiveAt(closingTimestamp)) {
                contract.close(closingTimestamp);
                affectedContainers.add(contract.getChannelsContainer());
            }
        });
        effectiveContracts.removeAll(contractsToRemove);
        eventService.postEvent(EventType.CHANNELS_CONTAINERS_CLIPPED.topic(), new EventType.ChannelsContainersClippedEvent(affectedContainers));
    }

    @Override
    public List<ReadingTypeRequirement> getReadingTypeRequirements() {
        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
        this.effectiveContracts
                .stream()
                .filter(emct -> !emct.getRange().isEmpty())
                .map(EffectiveMetrologyContractOnUsagePoint::getMetrologyContract)
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .map(ReadingTypeDeliverable::getFormula)
                .map(Formula::getExpressionNode)
                .forEach(expressionNode -> expressionNode.accept(requirementsCollector));
        return requirementsCollector.getReadingTypeRequirements();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("usagePoint", this.usagePoint)
                .add("configuration", this.metrologyConfiguration)
                .toString();
    }

    public void prepareDelete() {
        effectiveContracts.clear();
    }

    @Override
    public Instant getStart() {
        return getRange().lowerEndpoint();
    }

    @Override
    public Instant getEnd() {
        Range<Instant> range = getRange();
        return range.hasUpperBound() ? range.upperEndpoint() : null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public boolean isComplete(MetrologyContract metrologyContract) {
        UsagePointMetrologyConfiguration configuration = this.metrologyConfiguration.get();
        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
        metrologyContract.getDeliverables()
                .stream()
                .map(ReadingTypeDeliverable::getFormula)
                .map(Formula::getExpressionNode)
                .forEach(expressionNode -> expressionNode.accept(requirementsCollector));

        List<MeterRole> meterRoles = requirementsCollector.getReadingTypeRequirements()
                .stream()
                .map(configuration::getMeterRoleFor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean allMeterRolesHasMeters = true;
        for (MeterRole meterRole : meterRoles) {
            MeterActivation meterActivation;
            if (!usagePoint.get().getMeterActivations(meterRole).isEmpty()) {
                meterActivation = usagePoint.get().getMeterActivations(meterRole)
                        .stream()
                        .filter(meterActivationToCheck -> meterActivationToCheck.getEnd() == null)
                        .findFirst()
                        .orElse(null);
            } else {
                meterActivation = null;
            }
            allMeterRolesHasMeters &= meterActivation != null;
        }
        return allMeterRolesHasMeters;
    }
}
