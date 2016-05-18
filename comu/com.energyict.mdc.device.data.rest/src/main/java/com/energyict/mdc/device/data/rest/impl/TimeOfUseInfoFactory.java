package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.configuration.rest.impl.OptionInfo;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TimeOfUseInfoFactory {
    private Thesaurus thesaurus;
    private CalendarInfoFactory calendarInfoFactory;
    private DeviceConfigurationService deviceConfigurationService;

    @Inject
    public TimeOfUseInfoFactory(Thesaurus thesaurus, DeviceConfigurationService deviceConfigurationService) {
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public TimeOfUseInfo from(Optional<ActiveEffectiveCalendar> activeCalendar, List<PassiveEffectiveCalendar> passiveCalendars, Device device, CalendarInfoFactory calendarInfoFactory) {
        TimeOfUseInfo info = new TimeOfUseInfo();
        this.calendarInfoFactory = calendarInfoFactory;
        if(activeCalendar.isPresent()) {
            if(activeCalendar.get().getAllowedCalendar().getCalendar().isPresent()) {
                info.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
            } else {
                info.activeCalendar =  this.calendarInfoFactory.nameOnly(activeCalendar.get().getAllowedCalendar().getName());
                info.activeIsGhost = true;
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

        info.supportedOptions = getOptions(device);
        return info;
    }

    private List<String> getOptions(Device device) {
        Set<ProtocolSupportedCalendarOptions> supportedCalendarOptions = deviceConfigurationService.getSupportedTimeOfUseOptionsFor(device.getDeviceConfiguration().getDeviceType(), true);
        Optional<TimeOfUseOptions> timeOfUseOptions = deviceConfigurationService.findTimeOfUseOptions(device.getDeviceConfiguration().getDeviceType());
        Set<ProtocolSupportedCalendarOptions> allowedOptions = timeOfUseOptions.map(TimeOfUseOptions::getOptions).orElse(Collections
                .emptySet());
        if(supportedCalendarOptions.contains(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR)) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.VERIFY_ACTIVE_CALENDAR);
        }

        return allowedOptions.stream()
                .map(ProtocolSupportedCalendarOptions::getId)
                .collect(Collectors.toList());
    }

    private int compareNextExecution(PassiveEffectiveCalendar p1, PassiveEffectiveCalendar p2) {
        return p1.getComTaskExecution().get().getNextExecutionTimestamp().compareTo(p2.getComTaskExecution().get().getNextExecutionTimestamp());
    }
}
