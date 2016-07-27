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

    public static DeviceConfigurationChangeException noDeviceConfigChangeInActionFound(Thesaurus thesaurus, long deviceConfigChangeInActionId) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.NO_DEVICE_CONFIG_CHANGE_BUSINESS_LOCK_FOUND, deviceConfigChangeInActionId);
        deviceConfigurationChangeException.set("deviceConfigChangeInActionId", deviceConfigChangeInActionId);
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

    public static DeviceConfigurationChangeException needToSearchOnDeviceConfigForBulkAction(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_CONFIG);
    }

    public static DeviceConfigurationChangeException needToSearchOnSingleDeviceConfigForBulkAction(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.BULK_CHANGE_CONFIG_ONLY_ON_SEARCH_OF_UNIQUE_CONFIG);
    }

    public static DeviceConfigurationChangeException invalidSearchValueForBulkConfigChange(Thesaurus thesaurus, String propertyName) {
        DeviceConfigurationChangeException deviceConfigurationChangeException = new DeviceConfigurationChangeException(thesaurus, MessageSeeds.BULK_CHANGE_CONFIG_INVALID_SEARCH_VALUE, propertyName);
        deviceConfigurationChangeException.set("propertyName", propertyName);
        return deviceConfigurationChangeException;
    }

    public static DeviceConfigurationChangeException cannotChangeConfigOfDataLoggerSlave(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_CONFIG_DATALOGGER_SLAVE);
    }

    public static DeviceConfigurationChangeException cannotChangeConfigOfDataLoggerEnabledDevice(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_CONFIG_FROM_DATALOGGER_ENABLED);
    }

    public static DeviceConfigurationChangeException cannotchangeConfigToDataLoggerEnabled(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_CONFIG_TO_DATALOGGER_ENABLED);
    }

    @Override
    public DeviceConfigurationChangeException get() {
        return this;
    }
}
