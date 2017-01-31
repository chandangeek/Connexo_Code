/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.EventSet;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CalendarImportResult {

    private final List<EventSet> createdEventSets;
    private final List<Calendar> calendars;

    public CalendarImportResult(Iterable<EventSet> createdEventSets, List<Calendar> calendars) {
        this.createdEventSets = ImmutableList.copyOf(createdEventSets);
        this.calendars = ImmutableList.copyOf(calendars);
    }

    public List<EventSet> getEventSets() {
        return createdEventSets;
    }

    public List<Calendar> getCalendars() {
        return calendars;
    }
}
