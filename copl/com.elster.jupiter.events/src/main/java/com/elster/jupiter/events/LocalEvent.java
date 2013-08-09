package com.elster.jupiter.events;

import org.osgi.service.event.Event;

import java.util.Date;

public interface LocalEvent {

    Date getDateTime();

    EventType getType();

    Event toOsgiEvent();

    void publish();

    Object getSource();
}
