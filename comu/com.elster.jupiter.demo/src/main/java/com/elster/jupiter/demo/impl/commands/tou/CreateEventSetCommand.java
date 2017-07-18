/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.tou;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventSet;

import javax.inject.Inject;

/**
 * Creates the peak/offpeak {@link EventSet} for the Belgian electricity market.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-17 (13:11)
 */
class CreateEventSetCommand {

    static final String EVENT_SET_NAME = "Peak/Offpeak (Belgium)";

    private final CalendarService calendarService;

    @Inject
    CreateEventSetCommand(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    EventSet createEventSet() {
        return this.calendarService
                .newEventSet(EVENT_SET_NAME)
                .addEvent("Peak").withCode(EventCodes.PEAK.getCode())
                .addEvent("Offpeak").withCode(EventCodes.OFFPEAK.getCode())
                .add();
    }

}