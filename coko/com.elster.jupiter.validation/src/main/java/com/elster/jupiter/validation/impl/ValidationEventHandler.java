/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.validation.validationeventhandler", service = Subscriber.class, immediate = true)
public class ValidationEventHandler extends EventHandler<LocalEvent> {
    private static final String CREATED_TOPIC = EventType.READINGS_CREATED.topic();
    private static final String REMOVED_TOPIC = EventType.READINGS_DELETED.topic();
    private static final String ADVANCED_TOPIC = EventType.METER_ACTIVATION_ADVANCED.topic();

    private volatile ValidationServiceImpl validationService;

    public ValidationEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = (ValidationServiceImpl) validationService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(CREATED_TOPIC)) {
            ReadingStorer storer = (ReadingStorer) event.getSource();
            StorerProcess action = storer.getStorerProcess();
            if (StorerProcess.CONFIRM != action) {
                Map<ChannelsContainer, Map<Channel, Range<Instant>>> scopePerChannelPerChannelsContainer
                        = determineScopePerChannelPerChannelsContainer(storer);
                Map<Channel, Range<Instant>> dependentScope = scopePerChannelPerChannelsContainer.entrySet().stream()
                        .flatMap(containerAndScopeByChannelMap -> containerAndScopeByChannelMap.getKey()
                                .findDependentChannelScope(containerAndScopeByChannelMap.getValue())
                                .entrySet()
                                .stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Range::span));
                resetEstimatedReadingsOnDependentScope(dependentScope);
                if (StorerProcess.ESTIMATION != action) {
                    scopePerChannelPerChannelsContainer.entrySet()
                            .forEach(containerAndScopeByChannel -> validationService.validate(containerAndScopeByChannel.getKey(),
                                    containerAndScopeByChannel.getValue()));
                }
                validationService.validate(dependentScope);
            }
        } else if (event.getType().getTopic().equals(REMOVED_TOPIC)) {
            Channel.ReadingsDeletedEvent deleteEvent = (Channel.ReadingsDeletedEvent) event.getSource();
            Channel channel = deleteEvent.getChannel();
            ChannelsContainer channelsContainer = channel.getChannelsContainer();
            Map<Channel, Range<Instant>> scope = ImmutableMap.of(channel, deleteEvent.getRange());
            Map<Channel, Range<Instant>> dependentScope = channelsContainer.findDependentChannelScope(scope);
            resetEstimatedReadingsOnDependentScope(dependentScope);
            validationService.validate(channelsContainer, scope);
            validationService.validate(dependentScope);
        } else if (event.getType().getTopic().equals(ADVANCED_TOPIC)) {
            handleAdvancedMeterActivation((EventType.MeterActivationAdvancedEvent) event.getSource());
        }
    }

    private static Map<ChannelsContainer, Map<Channel, Range<Instant>>> determineScopePerChannelPerChannelsContainer(ReadingStorer storer) {
        return storer.getScope().entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getChannelContainer(),
                        Collectors.toMap(entry -> entry.getKey().getChannel(), Map.Entry::getValue, Range::span)));
    }

    private static void resetEstimatedReadingsOnDependentScope(Map<Channel, Range<Instant>> dependentScope) {
        sortDependentScopeByDependency(dependentScope).forEach(channelAndScope ->
                resetEstimatedReadings(channelAndScope.getKey(), makeEndless(channelAndScope.getValue())));
    }

    private static Stream<Map.Entry<Channel, Range<Instant>>> sortDependentScopeByDependency(Map<Channel, Range<Instant>> scope) {
        return scope.entrySet().stream()
                .collect(Collectors.groupingBy(channelAndScope -> channelAndScope.getKey().getChannelsContainer()))
                .entrySet().stream()
                .flatMap(containerAndChannelsWithScopes -> {
                    ChannelsContainer container = containerAndChannelsWithScopes.getKey();
                    if (container instanceof MetrologyContractChannelsContainer) {
                        List<ReadingType> sortedReadingTypes = ((MetrologyContractChannelsContainer) container)
                                .getMetrologyContract()
                                .sortReadingTypesByDependency();
                        return containerAndChannelsWithScopes.getValue().stream()
                                .sorted(basedOnList(sortedReadingTypes, readingTypeExtractor()).reversed());
                    } else {
                        // unreachable case
                        return containerAndChannelsWithScopes.getValue().stream();
                    }
                });
    }

    private static <T, U> Comparator<T> basedOnList(List<? extends U> reference, Function<? super T, ? extends U> keyExtractor) {
        return Comparator.comparing(keyExtractor, Comparator.comparing(reference::indexOf));
    }

    private static Function<Map.Entry<Channel, Range<Instant>>, ReadingType> readingTypeExtractor() {
        return channelAndScope -> channelAndScope.getKey().getMainReadingType();
    }

    private static void resetEstimatedReadings(Channel channel, Range<Instant> interval) {
        channel.removeReadings(QualityCodeSystem.MDM, findEstimatedReadings(channel, interval));
    }

    private static List<BaseReadingRecord> findEstimatedReadings(Channel channel, Range<Instant> interval) {
        return channel.findReadingQualities()
                .inTimeInterval(interval)
                .actual()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofAnyQualityIndexInCategory(QualityCodeCategory.ESTIMATED)
                .stream()
                .map(ReadingQualityRecord::getReadingTimestamp)
                .map(channel::getReading)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    private static Range<Instant> makeEndless(Range<Instant> range) {
        return Ranges.copy(range).withoutUpperBound();
    }

    private void handleAdvancedMeterActivation(EventType.MeterActivationAdvancedEvent advanceEvent) {
        validationService.getPersistedChannelsContainerValidations(advanceEvent.getAdvanced().getChannelsContainer())
                .forEach(channelsContainerValidation -> channelsContainerValidation
                        .updateLastChecked(advanceEvent.getAdvanced().getStart()));
        if (advanceEvent.getShrunk() != null) {
            Instant rightAfterNewLastChecked = advanceEvent.getShrunk().getEnd().plusMillis(1);
            validationService.getPersistedChannelsContainerValidations(advanceEvent.getShrunk().getChannelsContainer())
                    .forEach(channelsContainerValidation -> channelsContainerValidation
                            .moveLastCheckedBefore(rightAfterNewLastChecked));
        }
    }
}
