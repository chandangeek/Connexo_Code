/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;

public class UpdateEventTypeTransaction implements Transaction<EventType> {
	
	private final EventTypeInfo info;
    private final EventService eventService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    public UpdateEventTypeTransaction(EventTypeInfo info, EventService eventService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.info = info;
        this.eventService = eventService;
        this.conflictFactory = conflictFactory;
    }

    @Override
    public EventType perform() {
    	EventType eventType = fetchEventType();
        return doUpdate(eventType);
    }

    private EventType doUpdate(EventType eventType) {
        info.updateEventType(eventType);
        eventType.update();
        return eventType;
    }

    private EventType fetchEventType() {
        return eventService.findAndLockEventTypeByNameAndVersion(info.topic, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> eventService.getEventType(info.topic).map(EventType::getVersion).orElse(null))
                        .supplier());
    }
}
