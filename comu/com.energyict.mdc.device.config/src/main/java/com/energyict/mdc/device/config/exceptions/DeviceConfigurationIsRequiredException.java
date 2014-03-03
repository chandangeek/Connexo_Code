package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.device.config.DeviceConfiguration}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 13:30
 */
public class DeviceConfigurationIsRequiredException extends LocalizedException {

    private DeviceConfigurationIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new DeviceConfigurationIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterSpec} without a
     * {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return the newly create DeviceConfigurationIsRequiredException
     */
    public static DeviceConfigurationIsRequiredException registerSpecRequiresDeviceConfig(Thesaurus thesaurus) {
        return new DeviceConfigurationIsRequiredException(thesaurus, MessageSeeds.REGISTER_SPEC_DEVICE_CONFIG_IS_REQUIRED);
    }

    /**
     * Creates a new DeviceConfigurationIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LoadProfileSpec} without a
     * {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return the newly create DeviceConfigurationIsRequiredException
     */
    public static DeviceConfigurationIsRequiredException loadProfileSpecRequiresDeviceConfig(Thesaurus thesaurus) {
        return new DeviceConfigurationIsRequiredException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_DEVICE_CONFIG_IS_REQUIRED);
    }

    /**
     * Creates a new DeviceConfigurationIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without a
     * {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return the newly create DeviceConfigurationIsRequiredException
     */
    public static DeviceConfigurationIsRequiredException channelSpecRequiresDeviceConfig(Thesaurus thesaurus) {
        return new DeviceConfigurationIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_DEVICE_CONFIG_IS_REQUIRED);
    }

    public static DeviceConfigurationIsRequiredException logBookSpecRequiresDeviceConfig(Thesaurus thesaurus) {
        return new DeviceConfigurationIsRequiredException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_DEVICE_CONFIG_IS_REQUIRED);

    }
}
