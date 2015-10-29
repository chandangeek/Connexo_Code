package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.function.Supplier;

/**
 * Models an exception which occurs when one tries to change the configuration of a device
 */
public class DeviceConfigurationChangeException extends LocalizedException implements Supplier<DeviceConfigurationChangeException> {

    private DeviceConfigurationChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

    public static DeviceConfigurationChangeException cannotChangeToSameConfig(Thesaurus thesaurus, Device device) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG, device);
        deviceConfigurationChangeException.set("device", device);
        return deviceConfigurationChangeException;
    }

    public static DeviceConfigurationChangeException cannotChangeToConfigOfOtherDeviceType(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE);
    }

    public static DeviceConfigurationChangeException noDestinationConfigFoundForVersion(Thesaurus thesaurus, long destinationDeviceConfig, long destinationDeviceConfigVersion) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.NO_DESTINATION_DEVICE_CONFIG_FOUND_FOR_VERSION, destinationDeviceConfig, destinationDeviceConfigVersion);
        deviceConfigurationChangeException.set("destinationDeviceConfig", destinationDeviceConfig);
        deviceConfigurationChangeException.set("destinationDeviceConfigVersion", destinationDeviceConfigVersion);
        return deviceConfigurationChangeException;
    }

    public static DeviceConfigurationChangeException noDeviceConfigChangeRequestFound(Thesaurus thesaurus, long deviceConfigChangeRequestId) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND, deviceConfigChangeRequestId);
        deviceConfigurationChangeException.set("deviceConfigChangeRequestId", deviceConfigChangeRequestId);
        return deviceConfigurationChangeException;
    }

    public static DeviceConfigurationChangeException noDeviceFoundForConfigChange(Thesaurus thesaurus, String deviceMrid) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND, deviceMrid);
        deviceConfigurationChangeException.set("deviceMrid", deviceMrid);
        return deviceConfigurationChangeException;
    }

    public static DeviceConfigurationChangeException noDeviceFoundForVersion(Thesaurus thesaurus, long deviceId, long deviceVersion) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.INCORRECT_DEVICE_VERSION, deviceId, deviceVersion);
        deviceConfigurationChangeException.set("deviceId", deviceId);
        deviceConfigurationChangeException.set("deviceVersion", deviceVersion);
        return deviceConfigurationChangeException;
    }

    @Override
    public DeviceConfigurationChangeException get() {
        return this;
    }
}
