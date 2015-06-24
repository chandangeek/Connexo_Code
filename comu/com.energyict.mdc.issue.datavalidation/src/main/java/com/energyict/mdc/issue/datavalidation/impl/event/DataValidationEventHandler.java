package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.Injector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class DataValidationEventHandler implements MessageHandler {
    
    public static final Logger LOGGER = Logger.getLogger(DataValidationEventHandler.class.getName());

    private Injector injector;
    private JsonService jsonService;
    private IssueCreationService issueCreationService;

    public DataValidationEventHandler(Injector injector) {
        this.injector = injector;
        this.jsonService = injector.getInstance(JsonService.class);
        this.issueCreationService = injector.getInstance(IssueCreationService.class);
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        Optional<IssueEvent> event = createEvent(map);
        if (event.isPresent()) {
            issueCreationService.dispatchCreationEvent(Collections.singletonList(event.get()));
        }
    }

    private Optional<IssueEvent> createEvent(Map<?, ?> map) {
        return Arrays.asList(DataValidationEventDescription.values()).stream()
                .filter(eventDescription -> eventDescription.matches(map))
                .findFirst()
                .map(eventDescription -> createEventsBasedOnDescription(map, eventDescription))
                .orElse(Optional.empty());
    }

    private Optional<IssueEvent> createEventsBasedOnDescription(Map<?, ?> map, DataValidationEventDescription description) {
        DataValidationEvent event = injector.getInstance(description.getEventClass());
        try {
            event.init(map);
        } catch (UnableToCreateEventException e) {
            LOGGER.warning(e.getLocalizedMessage());
            return Optional.empty();
        }
        return Optional.of(event);
    }
}
