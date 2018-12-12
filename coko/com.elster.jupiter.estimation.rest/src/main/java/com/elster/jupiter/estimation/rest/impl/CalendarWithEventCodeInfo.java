/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;


import com.elster.jupiter.estimation.CalendarWithEventSettings;
import com.elster.jupiter.estimation.DiscardDaySettings;
import com.elster.jupiter.estimation.NoneCalendarWithEventSettings;


public class CalendarWithEventCodeInfo {

    public boolean discardDays;
    public Long calendar;
    public Long eventCode;

    public CalendarWithEventCodeInfo(){}

    public CalendarWithEventCodeInfo(CalendarWithEventSettings calendarWithEventSettings){
        if (calendarWithEventSettings instanceof NoneCalendarWithEventSettings){
            discardDays = false;
        }
        else {
            DiscardDaySettings settings = (DiscardDaySettings)calendarWithEventSettings;
            calendar = settings.getCalendar().getId();
            eventCode = settings.getEvent().getId();
            discardDays = settings.isDiscardDay();
        }

    }

    public String toString() {
        if (discardDays){
            return discardDays + ":" + calendar + ":" + eventCode;
        }
    else {
            return NoneCalendarWithEventSettings.NONE_CALENDAR_SETTINGS;
        }
    }
}
