package com.elster.jupiter.events.rest.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

public class UpdateEventTypeTransaction implements Transaction<EventType> {
	
	private final EventTypeInfo info;

    public UpdateEventTypeTransaction(EventTypeInfo info) {
        this.info = info;
    }

    @Override
    public EventType perform() {
    	EventType eventType = fetchEventType();
        validateUpdate(eventType);
        return doUpdate(eventType);
    }

    private EventType doUpdate(EventType eventType) {
        info.updateEventType(eventType);
        eventType.save();
        return eventType;
    }
    
    private void validateUpdate(EventType eventType) {

    }

    private EventType fetchEventType() {
        Optional<EventType> eventType = Bus.getEventService().getEventType(info.topic);
        if (eventType.isPresent()) {
            return eventType.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }


}
