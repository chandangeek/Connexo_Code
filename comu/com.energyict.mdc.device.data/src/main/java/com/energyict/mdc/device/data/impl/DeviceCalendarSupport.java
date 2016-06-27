package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendarInformation;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link Device.CalendarSupport} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-24 (16:15)
 */
class DeviceCalendarSupport implements Device.CalendarSupport {

    private final DeviceImpl device;
    private final DataModel dataModel;
    private final Clock clock;

    DeviceCalendarSupport(DeviceImpl device, DataModel dataModel, Clock clock) {
        this.device = device;
        this.dataModel = dataModel;
        this.clock = clock;
    }

    @Override
    public Optional<ActiveEffectiveCalendar> getActive() {
        return this.device.activeEffectiveCalendar()
                .effective(this.clock.instant())
                .map(ActiveEffectiveCalendar.class::cast);
    }

    @Override
    public List<PassiveEffectiveCalendar> getPassive() {
        return Collections.unmodifiableList(this.device.getPassiveCalendars());
    }

    @Override
    public void addPassive(AllowedCalendar passiveCalendar, Instant activationDate, DeviceMessage deviceMessage) {
        PassiveEffectiveCalendarImpl passiveEffectiveCalendar = new PassiveEffectiveCalendarImpl();
        passiveEffectiveCalendar.setDeviceMessage(deviceMessage);
        this.addPassiveCalendar(passiveCalendar, activationDate, passiveEffectiveCalendar);
    }

    @Override
    public void updateCalendars(CollectedCalendarInformation collectedData) {
        Instant now = this.clock.instant();
        if (is(collectedData.getActiveCalendar()).presentAndEqualTo(collectedData.getPassiveCalendar())) {
            this.setActiveCalendar(collectedData.getActiveCalendar().get(), now);
        } else {
            collectedData.getActiveCalendar().ifPresent(activeCalendarName -> this.setActiveCalendar(activeCalendarName, now));
            collectedData.getPassiveCalendar().ifPresent(passiveCalendarName -> this.setPassiveCalendar(passiveCalendarName, now));
        }
    }

    private void setActiveCalendar(String calendarName, Instant now) {
        Optional<AllowedCalendar> allowedCalendar = this.getAllowedCalendar(this.device, calendarName);
        if (allowedCalendar.isPresent()) {
            this.setActiveCalendar(allowedCalendar.get(), now);
        } else {
            this.setActiveCalendar(
                    this.device.getDeviceType().addGhostCalendar(calendarName),
                    now);
        }
    }

    private void setActiveCalendar(AllowedCalendar calendar, Instant now) {
        this.setActiveCalendar(calendar, now, now);
        Iterator<PassiveEffectiveCalendar> passiveCalendars = this.device.getPassiveCalendars().iterator();
        while (passiveCalendars.hasNext()) {
            PassiveEffectiveCalendar each = passiveCalendars.next();
            if (each.getAllowedCalendar().getId() == calendar.getId()) {
                passiveCalendars.remove();
            }
        }
    }

    private void setActiveCalendar(AllowedCalendar allowedCalendar, Instant effective, Instant lastVerified) {
        Interval effectivityInterval = Interval.of(Range.atLeast(effective));
        this.closeCurrentActiveCalendar(effective);
        this.createNewActiveCalendar(allowedCalendar, lastVerified, effectivityInterval);
        if (this.device.getId() != 0) {
            this.dataModel.touch(this.device);
        }
    }

    private void closeCurrentActiveCalendar(Instant now) {
        this.device.activeEffectiveCalendar().effective(now).ifPresent(active -> active.close(now));
    }

    private void createNewActiveCalendar(AllowedCalendar allowedCalendar, Instant lastVerified, Interval effectivityInterval) {
        ServerActiveEffectiveCalendar newCalendar =
                this.dataModel
                        .getInstance(ActiveEffectiveCalendarImpl.class)
                        .initialize(effectivityInterval, this.device, allowedCalendar, lastVerified);
        this.device.activeEffectiveCalendar().add(newCalendar);
    }

    private void setPassiveCalendar(String calendarName, Instant now) {
        if (this.notPassiveYet(calendarName)) {
            Optional<AllowedCalendar> allowedCalendar = this.getAllowedCalendar(this.device, calendarName);
            if (allowedCalendar.isPresent()) {
                this.addPassiveCalendar(allowedCalendar.get(), now);
            } else {
                this.addPassiveCalendar(this.device.getDeviceType().addGhostCalendar(calendarName), now);
            }
        }
    }

    private boolean notPassiveYet(String calendarName) {
        return !this.device.getPassiveCalendars()
                    .stream()
                    .map(PassiveEffectiveCalendar::getAllowedCalendar)
                    .map(AllowedCalendar::getName)
                    .anyMatch(allowedCalendarName -> allowedCalendarName.equals(calendarName));
    }

    private void addPassiveCalendar(AllowedCalendar passiveCalendar, Instant now) {
        this.addPassiveCalendar(passiveCalendar, now, new PassiveEffectiveCalendarImpl());
    }

    private void addPassiveCalendar(AllowedCalendar calendar, Instant activationDate, PassiveEffectiveCalendarImpl passiveEffectiveCalendar) {
        if (!this.device.getDeviceType().getAllowedCalendars().stream().anyMatch(each -> each.equals(calendar))) {
            throw new IllegalArgumentException("Calendar is not allowed on device type");
        }
        passiveEffectiveCalendar.setAllowedCalendar(calendar);
        passiveEffectiveCalendar.setActivationDate(activationDate);
        passiveEffectiveCalendar.setDevice(this.device);
        this.device.getPassiveCalendars().add(passiveEffectiveCalendar);
    }

    private Optional<AllowedCalendar> getAllowedCalendar(Device device, String calendarName) {
        return device
                .getDeviceType()
                .getAllowedCalendars()
                .stream()
                .filter(each -> each.getName().equals(calendarName))
                .findFirst();
    }

}