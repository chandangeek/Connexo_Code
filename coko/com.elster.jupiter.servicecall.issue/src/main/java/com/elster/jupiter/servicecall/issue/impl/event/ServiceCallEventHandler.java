/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Injector;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ServiceCallEventHandler implements MessageHandler {

    public static final Logger LOGGER = Logger.getLogger(ServiceCallEventHandler.class.getName());

    private Injector injector;
    private JsonService jsonService;
    private IssueCreationService issueCreationService;

    public ServiceCallEventHandler(Injector injector) {
        this.injector = injector;
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueCreationService = injector.getInstance(IssueCreationService.class);
    }

    @Override
    public void process(Message message) {
//        createEvent(jsonService.deserialize(message.getPayload(), Map.class))
//                .filter(e -> e.getEndDevice().isPresent())
//                .filter(e -> e.getEndDevice().get().getState().isPresent())
//                .filter(e -> e.getEndDevice().get().getState().get().getStage().isPresent())
//                .filter(e -> e.getEndDevice().get().getState().get().getStage().get().getName().equals(EndDeviceStage.OPERATIONAL.getKey()))
//                .ifPresent(event -> issueCreationService.dispatchCreationEvent(Collections.singletonList(event)));
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        return Arrays.asList(ServiceCallEventDescription.values()).stream()
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .map(eventDescription -> createEventsBasedOnDescription(map, eventDescription))
                .orElse(Optional.empty());
    }

    private Optional<IssueEvent> createEventsBasedOnDescription(Map<?, ?> map, ServiceCallEventDescription description) {
        ServiceCallEvent event = injector.getInstance(description.getEventClass());
        try {
            event.init(map);
        } catch (UnableToCreateEventException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }
}
