package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class UpdateEventTypeTransaction implements Transaction<EventType> {
	
	private final EventTypeInfo info;
    private final EventService eventService;

    public UpdateEventTypeTransaction(EventTypeInfo info, EventService eventService) {
        this.info = info;
        this.eventService = eventService;
    }

    @Override
    public EventType perform() {
    	EventType eventType = fetchEventType();
        validateUpdate(eventType);
        return doUpdate(eventType);
    }

    private EventType doUpdate(EventType eventType) {
        info.updateEventType(eventType);
        eventType.update();
        return eventType;
    }
    
    private void validateUpdate(EventType eventType) {

    }

    private EventType fetchEventType() {
        Optional<EventType> eventType = eventService.getEventType(info.topic);
        if (eventType.isPresent()) {
            return eventType.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }


}
