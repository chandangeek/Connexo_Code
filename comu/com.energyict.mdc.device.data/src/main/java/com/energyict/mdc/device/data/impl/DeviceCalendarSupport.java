/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendarInformation;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link Device.CalendarSupport} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-24 (16:15)
 */
class DeviceCalendarSupport implements Device.CalendarSupport {

    private final DataModel dataModel;
    private final Clock clock;

    private DeviceImpl device;

    DeviceCalendarSupport(DeviceImpl device, DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
        setDevice(device);
    }

    private void setDevice(Device device){
        this.device = (DeviceImpl) device;
    }

    @Override
    public Optional<ActiveEffectiveCalendar> getActive() {
        return this.device.activeEffectiveCalendar()
                .effective(this.clock.instant())
                .map(ActiveEffectiveCalendar.class::cast);
    }

    @Override
    public Optional<PassiveCalendar> getPassive() {
        return this.device.getPassiveCalendar();
    }

    @Override
    public Optional<PassiveCalendar> getPlannedPassive() {
        return this.device.getPlannedPassiveCalendar();
    }

    @Override
    public void setPassive(AllowedCalendar passiveCalendar, Instant activationDate, DeviceMessage deviceMessage) {
        this.validateCalendarAllowed(passiveCalendar);
        PassiveCalendarImpl passiveEffectiveCalendar = new PassiveCalendarImpl();
        passiveEffectiveCalendar.setDeviceMessage(deviceMessage);
        passiveEffectiveCalendar.setAllowedCalendar(passiveCalendar);
        passiveEffectiveCalendar.setActivationDate(activationDate);
        this.dataModel.persist(passiveEffectiveCalendar);
        this.device.setPlannedPassiveCalendar(passiveEffectiveCalendar);
        this.dataModel.update(this.device);
    }

    @Override
    public void updateCalendars(CollectedCalendarInformation collectedData) {
        Instant now = this.clock.instant();
        if (is(collectedData.getActiveCalendar()).presentAndEqualTo(collectedData.getPassiveCalendar())) {
            this.setActiveCalendar(collectedData.getActiveCalendar().get(), now);
            this.device.clearPassiveCalendar();
            this.device.clearPlannedPassiveCalendar();
        } else {
            collectedData.getActiveCalendar().ifPresent(activeCalendarName -> this.setActiveCalendar(activeCalendarName, now));
            collectedData.getPassiveCalendar().ifPresent(passiveCalendarName -> this.setPassiveCalendar(passiveCalendarName, now));
        }
        this.dataModel.update(this.device);
    }

    private void setActiveCalendar(String calendarName, Instant now) {
        Optional<AllowedCalendar> allowedCalendar = this.getAllowedCalendar(this.device, calendarName);
        if (allowedCalendar.isPresent()) {
            this.setActiveCalendar(allowedCalendar.get(), now);
        } else {
            setDevice(this.device.getDeviceService().lockDevice(this.device.getId()));
            DeviceType lockDeviceType = this.device.getLockService().lockDeviceType(this.device.getDeviceType().getId());
            Optional<AllowedCalendar> existingCalendar = lockDeviceType.getAllowedCalendars().stream().filter(allowedCalendar1 -> allowedCalendar1.getName().equals(calendarName)).findAny();
            if (existingCalendar.isPresent()) {
                this.setActiveCalendar(existingCalendar.get(), now);
            } else {
                this.setActiveCalendar(lockDeviceType.addGhostCalendar(calendarName), now);

            }
        }
    }

    private void setActiveCalendar(AllowedCalendar calendar, Instant now) {
        this.setActiveCalendar(calendar, now, now);
    }

    private void setActiveCalendar(AllowedCalendar allowedCalendar, Instant effective, Instant lastVerified) {
        Interval effectivityInterval = Interval.of(Range.atLeast(effective));
        Optional<ServerActiveEffectiveCalendar> activeEffectiveCalendar = this.device.activeEffectiveCalendar().effective(effective);
        Optional<AllowedCalendar> currentAllowedCalendar = activeEffectiveCalendar.map(ActiveEffectiveCalendar::getAllowedCalendar);
        if (currentAllowedCalendar.isPresent()) {
            if (!currentAllowedCalendar.get().equals(allowedCalendar)) {
                this.closeCurrentActiveCalendar(effective);
                this.createNewActiveCalendar(allowedCalendar, lastVerified, effectivityInterval);
                if (this.device.getId() != 0) {
                    this.dataModel.touch(this.device);
                }
            } else {
                this.getActive().ifPresent(activeCalendar -> activeCalendar.updateLastVerifiedDate(lastVerified));
            }
        } else {
            this.createNewActiveCalendar(allowedCalendar, lastVerified, effectivityInterval);
            if (this.device.getId() != 0) {
                this.dataModel.touch(this.device);
            }
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
                this.setPassiveCalendar(allowedCalendar.get(), now);
            } else {
                setDevice(this.device.getDeviceService().lockDevice(this.device.getId()));
                DeviceType lockDeviceType = this.device.getLockService().lockDeviceType(this.device.getDeviceType().getId());
                Optional<AllowedCalendar> existingCalendar = lockDeviceType.getAllowedCalendars().stream().filter(allowedCalendar1 -> allowedCalendar1.getName().equals(calendarName)).findAny();
                if (existingCalendar.isPresent()) {
                    this.setPassiveCalendar(existingCalendar.get(), now);
                } else {
                    this.setPassiveCalendar(lockDeviceType.addGhostCalendar(calendarName), now);
                }
            }
        }
    }

    private boolean notPassiveYet(String calendarName) {
        return !this.device.getPassiveCalendar()
                .map(PassiveCalendar::getAllowedCalendar)
                .map(AllowedCalendar::getName)
                .map(allowedCalendarName -> allowedCalendarName.equals(calendarName))
                .orElse(false);
    }

    private void setPassiveCalendar(AllowedCalendar passiveCalendar, Instant now) {
        this.validateCalendarAllowed(passiveCalendar);
        PassiveCalendarImpl passiveEffectiveCalendar = new PassiveCalendarImpl();
        passiveEffectiveCalendar.setAllowedCalendar(passiveCalendar);
        passiveEffectiveCalendar.setActivationDate(now);
        this.dataModel.persist(passiveEffectiveCalendar);
        this.device.setPassiveCalendar(passiveEffectiveCalendar);
        Optional<AllowedCalendar> plannedAllowedCalendar = this.device.getPlannedPassiveCalendar().map(PassiveCalendar::getAllowedCalendar);
        if (is(plannedAllowedCalendar).presentAndEqualTo(Optional.of(passiveCalendar))) {
            this.device.clearPlannedPassiveCalendar();
        }
    }

    private void validateCalendarAllowed(AllowedCalendar calendar) {
        if (!this.device.getDeviceType().getAllowedCalendars().stream().anyMatch(each -> each.equals(calendar))) {
            throw new IllegalArgumentException("Calendar is not allowed on device type");
        }
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