/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;

import com.sun.javafx.scene.layout.region.Margins;

public class DiscardDaySettings implements CalendarWithEventSettings {
    private boolean discardDay;
    private Calendar calendar;
    private Event event;

    public DiscardDaySettings(Boolean discardDay, Calendar calendar, Event event) {
        this.discardDay = discardDay;
        this.calendar = calendar;
        this.event = event;
    }

    public String toString() {
        if (discardDay && calendar != null && event != null) {
            return discardDay + ":" + calendar.getId() + ":" + event.getId();
        } else {
            return "";
        }
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public Long getCalendarId() {
        return this.calendar.getId();
    }

    public Event getEvent() {
        return this.event;
    }

    public Long getEventId() {
        return this.event.getId();
    }

    public Boolean isDiscardDay() {
        return this.discardDay;
    }
}
