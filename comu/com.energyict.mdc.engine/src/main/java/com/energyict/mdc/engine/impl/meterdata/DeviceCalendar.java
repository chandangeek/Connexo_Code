package com.energyict.mdc.engine.impl.meterdata;

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
public class DeviceCalendar extends CollectedDeviceData implements CollectedCalendar {

    private final DeviceIdentifier<?> deviceIdentifier;
    private Optional<String> activeCalendarName = Optional.empty();
    private Optional<String> passiveCalendarName = Optional.empty();
    private ComTaskExecution comTaskExecution;

    public DeviceCalendar(DeviceIdentifier deviceIdentifier) {
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
        return !this.activeCalendarName.isPresent() && !this.passiveCalendarName.isPresent();
    }

    @Override
    public Optional<String> getActiveCalendar() {
        return this.activeCalendarName;
    }

    @Override
    public void setActiveCalendar(String calendarName) {
        this.activeCalendarName = Optional.ofNullable(calendarName);
    }

    @Override
    public Optional<String> getPassiveCalendar() {
        return this.passiveCalendarName;
    }

    @Override
    public void setPassiveCalendar(String calendarName) {
        this.passiveCalendarName = Optional.ofNullable(calendarName);
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