/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.task.event.TaskEvent;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class TaskIssueEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(TaskIssueEventHandler.class.getName());
    private final Injector injector;

    public TaskIssueEventHandler(Injector injector) {
        this.injector = injector;
    }

    protected IssueCreationService getIssueCreationService() {
        return injector.getInstance(IssueCreationService.class);
    }

    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }

    protected Thesaurus getThesaurus() {
        return injector.getInstance(Thesaurus.class);
    }

    @Override
    public void process(Message message) {
        List<IssueEvent> events = createEvents(getJsonService().deserialize(message.getPayload(), Map.class));
        if (events != null && !events.isEmpty()) {
            //TODO: Filtering
            getIssueCreationService().dispatchCreationEvent(events);
        }
    }

    private List<IssueEvent> createEvents(Map<?, ?> map) {
        List<IssueEvent> events = new ArrayList<>();
                Arrays.stream(TaskEventDescription.values())
                .filter(description -> description.validateEvent(map))
                .forEach(description -> createEventsBasedOnDescription(events, map, description));
        return events;
    }

    private void createEventsBasedOnDescription(List<IssueEvent> events, Map<?, ?> map, EventDescription description) {
        for (Map<?, ?> mapForSingleEvent : description.splitEvents(map)) {
            TaskEvent tskEvent = injector.getInstance(description.getEventClass());
            try {
                tskEvent.init(map);
                // tskEvent.wrap(mapForSingleEvent, description);
                events.add(tskEvent);
            } catch (UnableToCreateIssueException e) {
                LOG.severe(e.getMessage());
            }
        }
    }

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents != null && contents instanceof Number) {
            return Optional.of(((Number) contents).longValue());
        }
        return Optional.empty();
    }
}
