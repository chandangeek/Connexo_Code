/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Injector;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ServiceCallMessageHandler implements MessageHandler {

    public static final Logger LOGGER = Logger.getLogger(ServiceCallMessageHandler.class.getName());

    private Injector injector;
    private JsonService jsonService;
    private IssueCreationService issueCreationService;

    public ServiceCallMessageHandler(Injector injector) {
        this.injector = injector;
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueCreationService = injector.getInstance(IssueCreationService.class);
    }

    @Override
    public void process(Message message) {
        createEvent(jsonService.deserialize(message.getPayload(), Map.class))
                .ifPresent(event -> issueCreationService.dispatchCreationEvent(Collections.singletonList(event)));
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        ServiceCallStateChangedEvent event = injector.getInstance(ServiceCallStateChangedEvent.class);
        try {
            event.init(map);
        } catch (UnableToCreateEventException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }

}
