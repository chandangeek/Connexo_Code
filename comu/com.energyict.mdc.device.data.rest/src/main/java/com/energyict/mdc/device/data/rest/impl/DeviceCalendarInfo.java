package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.util.conditions.Effective;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.EffectiveCalendar;

import java.util.ArrayList;
import java.util.List;

public class DeviceCalendarInfo {

    public List<String> passiveCalendars = new ArrayList<>();
    public CalendarInfo activeCalendar;
    private CalendarInfoFactory calendarInfoFactory;


    public DeviceCalendarInfo(List<EffectiveCalendar> effectiveCalendars, CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
        effectiveCalendars.stream()
                .forEach(this::checkEffectiveCalendar);
    }

    private void checkEffectiveCalendar(EffectiveCalendar effectiveCalendar) {
        if(ActiveEffectiveCalendar.class.isAssignableFrom(effectiveCalendar.getClass())) {
            if(!effectiveCalendar.getAllowedCalendar().isGhost()) {
                activeCalendar = calendarInfoFactory.detailedFromCalendar(effectiveCalendar.getAllowedCalendar().getCalendar().get());
            } else {
                activeCalendar = new CalendarInfo();
                activeCalendar.name = effectiveCalendar.getAllowedCalendar().getName();
            }
        } else {
            passiveCalendars.add(effectiveCalendar.getAllowedCalendar().getName());
        }
    }
}
