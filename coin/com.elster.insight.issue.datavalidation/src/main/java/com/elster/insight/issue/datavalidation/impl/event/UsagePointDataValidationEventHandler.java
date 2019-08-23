/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Injector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class UsagePointDataValidationEventHandler implements MessageHandler {

    public static final Logger LOGGER = Logger.getLogger(UsagePointDataValidationEventHandler.class.getName());

    private Injector injector;
    private JsonService jsonService;
    private IssueCreationService issueCreationService;

    public UsagePointDataValidationEventHandler(Injector injector) {
        this.injector = injector;
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueCreationService = injector.getInstance(IssueCreationService.class);
    }

    @Override
    public void process(Message message) {
        createEvent(jsonService.deserialize(message.getPayload(), Map.class))
                .filter(e -> e instanceof UsagePointDataValidationEvent)
                .filter(e -> ((UsagePointDataValidationEvent) e).getUsagePoint().isPresent())
                .filter(e -> ((UsagePointDataValidationEvent) e).getUsagePoint().get().getState() != null)
                .filter(e -> ((UsagePointDataValidationEvent) e).getUsagePoint().get().getState().getStage().isPresent())
                .filter(e -> ((UsagePointDataValidationEvent) e).getUsagePoint().get().getState().getStage().get().getName().equals(UsagePointStage.OPERATIONAL.getKey()))
                .ifPresent(event -> issueCreationService.dispatchCreationEvent(Collections.singletonList(event)));
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        return Arrays.asList(UsagePointDataValidationEventDescription.values()).stream()
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .map(eventDescription -> createEventsBasedOnDescription(map, eventDescription))
                .orElse(Optional.empty());
    }

    private Optional<IssueEvent> createEventsBasedOnDescription(Map<?, ?> map, UsagePointDataValidationEventDescription description) {
        UsagePointDataValidationEvent event = injector.getInstance(description.getEventClass());
        try {
            event.init(map);
        } catch (UnableToCreateIssueException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }
}
