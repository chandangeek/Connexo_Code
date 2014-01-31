package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 *
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 13:30
 */
public class DeviceConfigIsRequiredException extends LocalizedException {

    private DeviceConfigIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new DeviceConfigIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterSpec} without a
     * {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * @param thesaurus The Thesaurus
     * @return the newly create DeviceConfigIsRequiredException
     */
    public static DeviceConfigIsRequiredException registerSpecRequiresDeviceConfig(Thesaurus thesaurus){
        return new DeviceConfigIsRequiredException(thesaurus, MessageSeeds.REGISTER_SPEC_DEVICE_CONFIG_IS_REQUIRED);
    }
}
