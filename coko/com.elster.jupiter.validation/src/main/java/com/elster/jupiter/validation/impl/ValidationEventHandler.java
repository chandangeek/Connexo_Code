/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

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
                if (StorerProcess.ESTIMATION != action) {
                    scopePerChannelPerChannelsContainer.entrySet()
                            .forEach(containerAndScopeByChannel -> validationService.validate(containerAndScopeByChannel.getKey(),
                                    containerAndScopeByChannel.getValue()));
                }
                Map<Channel, Range<Instant>> dependentScope = scopePerChannelPerChannelsContainer.entrySet().stream()
                        .flatMap(containerAndScopeByChannelMap -> containerAndScopeByChannelMap.getKey()
                                .findDependentChannelScope(containerAndScopeByChannelMap.getValue())
                                .entrySet()
                                .stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Range::span));
                validationService.validate(dependentScope);
            }
        } else if (event.getType().getTopic().equals(REMOVED_TOPIC)) {
            Channel.ReadingsDeletedEvent deleteEvent = (Channel.ReadingsDeletedEvent) event.getSource();
            Channel channel = deleteEvent.getChannel();
            ChannelsContainer channelsContainer = channel.getChannelsContainer();
            Map<Channel, Range<Instant>> scope = ImmutableMap.of(channel, deleteEvent.getRange());
            validationService.validate(channelsContainer, scope);
            validationService.validate(channelsContainer.findDependentChannelScope(scope));
        } else if (event.getType().getTopic().equals(ADVANCED_TOPIC)) {
            handleAdvancedMeterActivation((EventType.MeterActivationAdvancedEvent) event.getSource());
        }
    }

    private static Map<ChannelsContainer, Map<Channel, Range<Instant>>> determineScopePerChannelPerChannelsContainer(ReadingStorer storer) {
        return storer.getScope().entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getChannelContainer(),
                        Collectors.toMap(entry -> entry.getKey().getChannel(), Map.Entry::getValue, Range::span)));
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
