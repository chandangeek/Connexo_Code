package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedFirmwareVersionDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * Straightforward implementation of a FirmwareVersion collectedData object.
 * By default all versions are empty.
 */
public class DeviceFirmwareVersion extends CollectedDeviceData implements CollectedFirmwareVersion {

    private final DeviceIdentifier<?> deviceDeviceIdentifier;
    private Optional<String> passiveCommunicationFirmwareVersion = Optional.empty();
    private Optional<String> activeCommunicationFirmwareVersion = Optional.empty();
    private Optional<String> passiveMeterFirmwareVersion = Optional.empty();
    private Optional<String> activeMeterFirmwareVersion = Optional.empty();

    public DeviceFirmwareVersion(DeviceIdentifier deviceIdentifier) {
        this.deviceDeviceIdentifier = deviceIdentifier;
    }

    @Override
    public Optional<String> getPassiveCommunicationFirmwareVersion() {
        return passiveCommunicationFirmwareVersion;
    }

    @Override
    public void setPassiveCommunicationFirmwareVersion(String passiveCommunicationFirmwareVersion) {
        this.passiveCommunicationFirmwareVersion = Optional.of(passiveCommunicationFirmwareVersion);
    }

    @Override
    public Optional<String> getActiveCommunicationFirmwareVersion() {
        return activeCommunicationFirmwareVersion;
    }

    @Override
    public void setActiveCommunicationFirmwareVersion(String activeCommunicationFirmwareVersion) {
        this.activeCommunicationFirmwareVersion = Optional.of(activeCommunicationFirmwareVersion);
    }

    @Override
    public Optional<String> getPassiveMeterFirmwareVersion() {
        return passiveMeterFirmwareVersion;
    }

    @Override
    public void setPassiveMeterFirmwareVersion(String passiveMeterFirmwareVersion) {
        this.passiveMeterFirmwareVersion = Optional.of(passiveMeterFirmwareVersion);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceDeviceIdentifier;
    }

    @Override
    public Optional<String> getActiveMeterFirmwareVersion() {
        return activeMeterFirmwareVersion;
    }

    @Override
    public void setActiveMeterFirmwareVersion(String activeMeterFirmwareVersion) {
        this.activeMeterFirmwareVersion = Optional.of(activeMeterFirmwareVersion);
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedFirmwareVersionDeviceCommand(serviceProvider,
                deviceDeviceIdentifier,
                activeMeterFirmwareVersion,
                passiveMeterFirmwareVersion,
                activeCommunicationFirmwareVersion,
                passiveCommunicationFirmwareVersion);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToReadStatusInformation();
    }
}
