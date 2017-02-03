/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import org.osgi.service.event.Event;

import java.time.Instant;

public interface LocalEvent {

    Instant getDateTime();

    EventType getType();

    Event toOsgiEvent();

    void publish();

    Object getSource();
}
