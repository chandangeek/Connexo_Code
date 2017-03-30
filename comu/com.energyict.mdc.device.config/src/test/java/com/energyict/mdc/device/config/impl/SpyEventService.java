/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.spy;

public class SpyEventService implements EventService {
    private final EventService eventService;

    public SpyEventService(EventService eventService) {
        super();
        this.eventService = spy(eventService);
    }

    @Override
    public void postEvent(String topic, Object source) {
        eventService.postEvent(topic, source);
    }

    @Override
    @TransactionRequired
    public EventTypeBuilder buildEventTypeWithTopic(String topic) {
        return eventService.buildEventTypeWithTopic(topic);
    }

    @Override
    public List<EventType> getEventTypes() {
        return eventService.getEventTypes();
    }

    @Override
    public Optional<EventType> getEventType(String topic) {
        return eventService.getEventType(topic);
    }

    @Override
    public Optional<EventType> findAndLockEventTypeByNameAndVersion(String topic, long version) {
        return eventService.findAndLockEventTypeByNameAndVersion(topic, version);
    }

    @Override
    public List<EventType> getEventTypesForComponent(String component) {
        return eventService.getEventTypesForComponent(component);
    }

    public EventService getSpy() {
        return eventService;
    }

}