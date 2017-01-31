/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.orm.TransactionRequired;

public enum EventType {

    CALENDAR_CREATE("CREATE"),
    CALENDAR_UPDATE("UPDATE"),
    CALENDAR_DELETE("DELETE");

    private static final String NAMESPACE = "com/elster/jupiter/calendar/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    public void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(CalendarService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .shouldNotPublish();
        addCustomProperties(builder).create();
    }

    EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }
}
