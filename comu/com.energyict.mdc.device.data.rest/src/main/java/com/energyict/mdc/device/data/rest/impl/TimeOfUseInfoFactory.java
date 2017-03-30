/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

public class TimeOfUseInfoFactory {
    private Thesaurus thesaurus;
    private CalendarInfoFactory calendarInfoFactory;
    private DeviceMessageService deviceMessageService;

    @Inject
    public TimeOfUseInfoFactory(Thesaurus thesaurus, CalendarInfoFactory calendarInfoFactory, DeviceMessageService deviceMessageService) {
        this.thesaurus = thesaurus;
        this.calendarInfoFactory = calendarInfoFactory;
        this.deviceMessageService = deviceMessageService;
    }

    public TimeOfUseInfo from(Device device) {
        return this.from(device.calendars(), device);
    }

    private TimeOfUseInfo from(Device.CalendarSupport calendarSupport, Device device) {
        return this.from(calendarSupport.getActive(), calendarSupport.getPassive(), calendarSupport.getPlannedPassive(), device);
    }

    private TimeOfUseInfo from(Optional<ActiveEffectiveCalendar> activeCalendar, Optional<PassiveCalendar> passiveCalendar, Optional<PassiveCalendar> plannedPassiveCalendar, Device device) {
        TimeOfUseInfo info = new TimeOfUseInfo();
        if (activeCalendar.isPresent() && !activeCalendar.get().getAllowedCalendar().isObsolete()) {
            if (activeCalendar.get().getAllowedCalendar().getCalendar().isPresent()) {
                info.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
            } else {
                info.activeCalendar =  this.calendarInfoFactory.nameOnly(activeCalendar.get().getAllowedCalendar().getName());
                info.activeIsGhost = true;
            }
            if (activeCalendar.get().getLastVerifiedDate() != null) {
                info.lastVerified = activeCalendar.get().getLastVerifiedDate().toEpochMilli();
            }
        }

        if (passiveCalendar.isPresent()) {
            this.addPassiveCalendar(info, new PassiveCalendarInfo(passiveCalendar.get().getAllowedCalendar().getName(), passiveCalendar.get().getAllowedCalendar().isGhost()));
        }

        if (plannedPassiveCalendar.isPresent()) {
            PassiveCalendar next = plannedPassiveCalendar.get();
            Instant activationDate = next.getActivationDate();
            boolean willBePickedUpByPlannedComtask = deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, next.getDeviceMessage().get());
            boolean willBePickedUpByComtask = willBePickedUpByPlannedComtask || deviceMessageService.willDeviceMessageBePickedUpByComTask(device, next.getDeviceMessage().get());
            info.nextPassiveCalendar =
                    new NextCalendarInfo(
                            next.getAllowedCalendar().getName(),
                            next.getDeviceMessage().get().getReleaseDate().toEpochMilli(),
                            (activationDate != null) ? activationDate.toEpochMilli() : 0,
                            DeviceMessageStatusTranslationKeys.translationFor(next.getDeviceMessage().get().getStatus(), thesaurus),
                            willBePickedUpByPlannedComtask,
                            willBePickedUpByComtask);
        }

        return info;
    }

    private void addPassiveCalendar(TimeOfUseInfo info, PassiveCalendarInfo passiveCalendar) {
        if (info.passiveCalendars == null) {
            info.passiveCalendars = new ArrayList<>();
        }
        info.passiveCalendars.add(passiveCalendar);
    }

}
