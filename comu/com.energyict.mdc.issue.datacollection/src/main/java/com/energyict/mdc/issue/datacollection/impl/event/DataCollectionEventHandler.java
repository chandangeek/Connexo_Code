package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.google.inject.Injector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DataCollectionEventHandler implements MessageHandler {
    private static final Logger LOG = Logger.getLogger(DataCollectionEventHandler.class.getName());
    private final Injector injector;

    public DataCollectionEventHandler(Injector injector) {
        this.injector = injector;
    }

    protected IssueCreationService getIssueCreationService() {
        return injector.getInstance(IssueCreationService.class);
    }

    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = getJsonService().deserialize(message.getPayload(), Map.class);
        List<IssueEvent> events = createEvents(map);
        if (events != null && !events.isEmpty()) {
            getIssueCreationService().dispatchCreationEvent(events);
        }
    }

    private List<IssueEvent> createEvents(Map<?, ?> map) {
        List<IssueEvent> events = new ArrayList<>();
        for (EventDescription description : DataCollectionEventDescription.values()) {
            if (description.validateEvent(map)) {
                createEventsBasedOnDescription(events, map, description);
            }
        }
        for (EventDescription description : DataCollectionResolveEventDescription.values()) {
            if (description.validateEvent(map)) {
                createEventsBasedOnDescription(events, map, description);
            }
        }
        return events;
    }

    private void createEventsBasedOnDescription(List<IssueEvent> events, Map<?, ?> map, EventDescription description) {
        for (Map<?, ?> mapForSingleEvent : description.splitEvents(map)) {
            DataCollectionEvent dcEvent = injector.getInstance(description.getEventClass());
            try {
                dcEvent.wrap(mapForSingleEvent, description);
                events.add(dcEvent);
            } catch (UnableToCreateEventException e) {
                LOG.severe(e.getMessage());
            }
        }
    }
}
