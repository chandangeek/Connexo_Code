package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public TimeOfUseInfo from(Optional<ActiveEffectiveCalendar> activeCalendar, List<PassiveEffectiveCalendar> passiveCalendars, Device device) {
        TimeOfUseInfo info = new TimeOfUseInfo();
        if(activeCalendar.isPresent()) {
            if(activeCalendar.get().getAllowedCalendar().getCalendar().isPresent()) {
                info.activeCalendar = this.calendarInfoFactory.detailedFromCalendar(activeCalendar.get().getAllowedCalendar().getCalendar().get());
            } else {
                info.activeCalendar =  this.calendarInfoFactory.nameOnly(activeCalendar.get().getAllowedCalendar().getName());
                info.activeIsGhost = true;
            }
            if(activeCalendar.get().getLastVerifiedDate() != null) {
                info.lastVerified = activeCalendar.get().getLastVerifiedDate().toEpochMilli();
            }
        }

        if (!passiveCalendars.isEmpty()) {

            info.passiveCalendars = new ArrayList<>();
            info.passiveCalendars = passiveCalendars.stream()
                    .distinct()
                    .map(PEC -> new PassiveCalendarInfo(PEC.getAllowedCalendar().getName(), PEC.getAllowedCalendar().isGhost()))
                    .collect(Collectors.toList());

            Optional<PassiveEffectiveCalendar> nextInLine = passiveCalendars.stream()
                    .filter(passiveCalendar -> passiveCalendar.getDeviceMessage().isPresent())
                    .sorted(this::compareCreationDate)
                    .findFirst();

            if(nextInLine.isPresent() && nextInLineDifferentFromActive(nextInLine.get(), activeCalendar)) {
                PassiveEffectiveCalendar next = nextInLine.get();
                Instant activationDate = next.getActivationDate();
                boolean willBePickedUpByPlannedComtask = deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, next.getDeviceMessage().get());
                boolean willBePickedUpByComtask = willBePickedUpByPlannedComtask || deviceMessageService.willDeviceMessageBePickedUpByComTask(device, next.getDeviceMessage().get());
                info.nextPassiveCalendar = new NextCalendarInfo(next.getAllowedCalendar().getName(), next.getDeviceMessage().get().getReleaseDate().toEpochMilli(),
                        (activationDate != null) ? activationDate.toEpochMilli() : 0, DeviceMessageStatusTranslationKeys.translationFor(next.getDeviceMessage().get().getStatus(), thesaurus),
                        willBePickedUpByPlannedComtask, willBePickedUpByComtask);
            }
        }

        return info;
    }

    private boolean nextInLineDifferentFromActive(PassiveEffectiveCalendar passiveEffectiveCalendar, Optional<ActiveEffectiveCalendar> optionalActiveEffectiveCalendar) {
        if(!optionalActiveEffectiveCalendar.isPresent()) {
            return true;
        }
        ActiveEffectiveCalendar activeEffectiveCalendar = optionalActiveEffectiveCalendar.get();
        if(activeEffectiveCalendar.getAllowedCalendar().isGhost()) {
            return true;
        }

        return !(passiveEffectiveCalendar.getAllowedCalendar().getName().equals(activeEffectiveCalendar.getAllowedCalendar().getName())
                && activeEffectiveCalendar.getAllowedCalendar().getCalendar().get().getVersion() == passiveEffectiveCalendar.getAllowedCalendar().getCalendar().get().getVersion()
                && (passiveEffectiveCalendar.getActivationDate() == null || passiveEffectiveCalendar.getActivationDate().isBefore(activeEffectiveCalendar.getLastVerifiedDate()))
                && passiveEffectiveCalendar.getDeviceMessage().get().getReleaseDate().isBefore(activeEffectiveCalendar.getLastVerifiedDate())
                && passiveEffectiveCalendar.getDeviceMessage().get().getStatus().equals(DeviceMessageStatus.CONFIRMED));
    }

    private int compareCreationDate(PassiveEffectiveCalendar p1, PassiveEffectiveCalendar p2) {
        return p2.getDeviceMessage().get().getCreationDate().compareTo(p1.getDeviceMessage().get().getCreationDate());
    }
}
