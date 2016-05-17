package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceCalendarInfo {

    public List<PassiveCalendarInfo> passiveCalendars;
    public CalendarInfo activeCalendar;
    private CalendarInfoFactory calendarInfoFactory;
    public long lastVerified;

    public DeviceCalendarInfo(Optional<ActiveEffectiveCalendar> activeCalendar, List<PassiveEffectiveCalendar> passiveCalendars, CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
        if(activeCalendar.isPresent()) {
            if(activeCalendar.get().getAllowedCalendar().getCalendar().isPresent()) {
                this.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
            } else {
                this.activeCalendar =  this.calendarInfoFactory.nameOnly(activeCalendar.get().getAllowedCalendar().getName());
            }
            this.lastVerified = activeCalendar.get().getLastVerifiedDate().toEpochMilli();
        }
        if(passiveCalendars.size() > 0) {
            this.passiveCalendars = new ArrayList<>();
            this.passiveCalendars = passiveCalendars.stream()
                    .map(PEC -> new PassiveCalendarInfo(PEC.getAllowedCalendar().getName(), PEC.getAllowedCalendar().isGhost()))
                    .collect(Collectors.toList());
        }
    }
}
