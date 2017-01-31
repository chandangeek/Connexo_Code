/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;

public interface EventSet extends HasName, HasId {

    /**
     * The List of {@link Event}s that can occur in Calendars using this EventSet.
     *
     * @return The List of Event
     */
    List<Event> getEvents();


    CalendarService.EventSetBuilder redefine();
}
