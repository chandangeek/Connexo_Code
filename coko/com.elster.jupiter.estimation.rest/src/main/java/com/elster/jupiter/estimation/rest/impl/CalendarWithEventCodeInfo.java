package com.elster.jupiter.estimation.rest.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.estimation.CalendarWithEventSettings;
import com.elster.jupiter.estimation.DiscardDayWithEventSettings;
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
            DiscardDayWithEventSettings settings = (DiscardDayWithEventSettings)calendarWithEventSettings;
            calendar = settings.getCalendar().getId();
            eventCode = settings.getEvent().getId();
            discardDays = settings.getDiscardDay();
        }

    }

    public String toString() {
        if (discardDays=true){
            return discardDays + ":" + calendar + ":" + eventCode;
        }
    else {
            return NoneCalendarWithEventSettings.NONE_CALENDAR_SETTINGS;
        }
    }
}
