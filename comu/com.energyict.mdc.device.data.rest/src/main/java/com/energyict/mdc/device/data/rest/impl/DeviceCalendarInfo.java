package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceCalendarInfo {

    public List<String> passiveCalendars;
    public CalendarInfo activeCalendar;
    private CalendarInfoFactory calendarInfoFactory;

    public DeviceCalendarInfo(Optional<ActiveEffectiveCalendar> activeCalendar, List<PassiveEffectiveCalendar> passiveCalendars, CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
        if(activeCalendar.isPresent()) {
            this.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
        }
        if(passiveCalendars.size() > 0) {
            this.passiveCalendars = new ArrayList<>();
            this.passiveCalendars = passiveCalendars.stream()
                    .map(PEC -> PEC.getAllowedCalendar().getName())
                    .collect(Collectors.toList());
        }
    }
}
