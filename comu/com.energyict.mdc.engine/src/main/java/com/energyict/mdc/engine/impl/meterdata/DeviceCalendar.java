/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedCalendarDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * Provides an implementation for the {@link CollectedCalendar} interface.
 */
class DeviceCalendar extends CollectedDeviceData implements CollectedCalendar {

    private final DeviceIdentifier<?> deviceIdentifier;
    private String activeCalendarName;
    private String passiveCalendarName;
    private ComTaskExecution comTaskExecution;

    DeviceCalendar(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        this.comTaskExecution = (ComTaskExecution) configuration;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    @Override
    public boolean isEmpty() {
        return this.activeCalendarName == null && this.passiveCalendarName == null;
    }

    @Override
    public Optional<String> getActiveCalendar() {
        return Optional.ofNullable(this.activeCalendarName);
    }

    @Override
    public void setActiveCalendar(String calendarName) {
        if (Checks.is(calendarName).empty()) {
            this.activeCalendarName = null;
        } else {
            this.activeCalendarName = calendarName;
        }
    }

    @Override
    public Optional<String> getPassiveCalendar() {
        return Optional.ofNullable(this.passiveCalendarName);
    }

    @Override
    public void setPassiveCalendar(String calendarName) {
        if (Checks.is(calendarName).empty()) {
            this.passiveCalendarName = null;
        } else {
            this.passiveCalendarName = calendarName;
        }
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedCalendarDeviceCommand(serviceProvider, this, comTaskExecution);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToReadStatusInformation();
    }

}