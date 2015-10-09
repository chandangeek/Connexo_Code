package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models an exception which occurs when one tries to change the configuration of a device
 */
public class DeviceConfigurationChangeException extends LocalizedException {

    private DeviceConfigurationChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments){
        super(thesaurus, messageSeed, arguments);
    }

    public static DeviceConfigurationChangeException cannotChangeToSameConfig(Thesaurus thesaurus, Device device){
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG, device);
    }

    public static DeviceConfigurationChangeException cannotChangeToConfigOfOtherDeviceType(Thesaurus thesaurus) {
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE);
    }

    public static DeviceConfigurationChangeException noDestinationConfigFoundForVersion(Thesaurus thesaurus, long destinationDeviceConfig, long destinationDeviceConfigVersion){
        return new DeviceConfigurationChangeException(thesaurus, MessageSeeds.NO_DESTINATION_DEVICE_CONFIG_FOUND_FOR_VERSION, destinationDeviceConfig,  destinationDeviceConfigVersion);
    }
}
