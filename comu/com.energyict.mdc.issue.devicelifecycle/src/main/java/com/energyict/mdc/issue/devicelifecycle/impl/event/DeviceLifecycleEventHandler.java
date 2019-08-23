/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.google.inject.Injector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class DeviceLifecycleEventHandler implements MessageHandler {
    
    public static final Logger LOGGER = Logger.getLogger(DeviceLifecycleEventHandler.class.getName());

    private Injector injector;
    private JsonService jsonService;
    private IssueCreationService issueCreationService;
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public DeviceLifecycleEventHandler(Injector injector) {
        this.injector = injector;
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueCreationService = injector.getInstance(IssueCreationService.class);
        this.deviceLifeCycleConfigurationService = injector.getInstance(DeviceLifeCycleConfigurationService.class);
    }

    @Override
    public void process(Message message) {
        createEvent(jsonService.deserialize(message.getPayload(), Map.class))
                .ifPresent(event -> issueCreationService.dispatchCreationEvent(Collections.singletonList(event)));
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        return Arrays.asList(DeviceLifecycleEventDescription.values()).stream()
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .map(eventDescription -> createEventsBasedOnDescription(map, eventDescription))
                .orElse(Optional.empty());
    }

    private Optional<IssueEvent> createEventsBasedOnDescription(Map<?, ?> map, DeviceLifecycleEventDescription description) {
        DeviceLifecycleEvent event = injector.getInstance(description.getEventClass());
        try {
            event.init(map);
        } catch (UnableToCreateIssueException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }
}
