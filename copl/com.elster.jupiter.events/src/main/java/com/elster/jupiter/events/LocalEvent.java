/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import aQute.bnd.annotation.ProviderType;
import org.osgi.service.event.Event;

import java.time.Instant;

@ProviderType
public interface LocalEvent {

    Instant getDateTime();

    EventType getType();

    Event toOsgiEvent();

    void publish();

    void publish(int delay);

    Object getSource();
}
