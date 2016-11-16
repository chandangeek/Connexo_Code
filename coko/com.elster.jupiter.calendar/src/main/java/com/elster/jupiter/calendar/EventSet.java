package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasName;

import java.util.List;

public interface EventSet extends HasName {

    /**
     * The List of {@link Event}s that can occur in Calendars using this EventSet.
     *
     * @return The List of Event
     */
    List<Event> getEvents();


}
