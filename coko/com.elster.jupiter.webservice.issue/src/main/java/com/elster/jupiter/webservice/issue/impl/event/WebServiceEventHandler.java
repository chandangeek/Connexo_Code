/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Inject;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class WebServiceEventHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(WebServiceEventHandler.class.getName());

    private final JsonService jsonService;
    private final IssueCreationService issueCreationService;
    private final Provider<WebServiceEvent> webServiceEventProvider;

    @Inject
    public WebServiceEventHandler(JsonService jsonService, IssueCreationService issueCreationService, Provider<WebServiceEvent> webServiceEventProvider) {
        this.jsonService = jsonService;
        this.issueCreationService = issueCreationService;
        this.webServiceEventProvider = webServiceEventProvider;
    }

    @Override
    public void process(Message message) {
        createEvent(jsonService.deserialize(message.getPayload(), Map.class))
                .filter(e -> e.getOccurrence().isPresent())
                .ifPresent(event -> issueCreationService.dispatchCreationEvent(Collections.singletonList(event)));
    }

    private Optional<WebServiceEvent> createEvent(Map<?, ?> map) {
        return Arrays.stream(WebServiceEventDescription.values())
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .flatMap(eventDescription -> createEventBasedOnDescription(eventDescription, map));
    }

    private Optional<WebServiceEvent> createEventBasedOnDescription(WebServiceEventDescription description, Map<?, ?> map) {
        WebServiceEvent event = webServiceEventProvider.get();
        try {
            event.init(description, map);
        } catch (UnableToCreateIssueException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }
}
