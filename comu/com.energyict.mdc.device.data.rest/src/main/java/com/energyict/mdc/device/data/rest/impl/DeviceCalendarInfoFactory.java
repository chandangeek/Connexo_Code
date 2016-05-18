package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceCalendarInfoFactory {
    private Thesaurus thesaurus;
    private CalendarInfoFactory calendarInfoFactory;

    @Inject
    public DeviceCalendarInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceCalendarInfo from(Optional<ActiveEffectiveCalendar> activeCalendar, List<PassiveEffectiveCalendar> passiveCalendars, CalendarInfoFactory calendarInfoFactory) {
        DeviceCalendarInfo info = new DeviceCalendarInfo();
        this.calendarInfoFactory = calendarInfoFactory;
        if(activeCalendar.isPresent()) {
            if(activeCalendar.get().getAllowedCalendar().getCalendar().isPresent()) {
                info.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
            } else {
                info.activeCalendar =  this.calendarInfoFactory.nameOnly(activeCalendar.get().getAllowedCalendar().getName());
            }
            info.lastVerified = activeCalendar.get().getLastVerifiedDate().toEpochMilli();
        }

        if(passiveCalendars.size() > 0) {

            info.passiveCalendars = new ArrayList<>();
            info.passiveCalendars = passiveCalendars.stream()
                    .map(PEC -> new PassiveCalendarInfo(PEC.getAllowedCalendar().getName(), PEC.getAllowedCalendar().isGhost()))
                    .collect(Collectors.toList());

            Optional<PassiveEffectiveCalendar> nextInLine = passiveCalendars.stream()
                    .filter(passiveCalendar -> passiveCalendar.getComTaskExecution().isPresent())
                    .sorted((p1, p2) -> compareNextExecution(p1, p2))
                    .findFirst();

            if(nextInLine.isPresent()) {
                PassiveEffectiveCalendar next = nextInLine.get();
                info.nextPassiveCalendar = new NextCalendarInfo(next.getAllowedCalendar().getName(), next.getComTaskExecution().get().getNextExecutionTimestamp().toEpochMilli(),
                        next.getActivationDate().toEpochMilli(), TaskStatusTranslationKeys.translationFor(next.getComTaskExecution().get().getStatus(), thesaurus));
            }
        }
        return info;
    }

    private int compareNextExecution(PassiveEffectiveCalendar p1, PassiveEffectiveCalendar p2) {
        return p1.getComTaskExecution().get().getNextExecutionTimestamp().compareTo(p2.getComTaskExecution().get().getNextExecutionTimestamp());
    }
}
