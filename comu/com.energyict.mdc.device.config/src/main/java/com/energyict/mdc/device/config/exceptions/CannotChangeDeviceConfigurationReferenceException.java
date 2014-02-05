package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.LoadProfileSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * of an existing object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 08:15
 */
public class CannotChangeDeviceConfigurationReferenceException extends LocalizedException {

    private CannotChangeDeviceConfigurationReferenceException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotChangeDeviceConfigurationReferenceException that models the exceptional
     * situation that occurs when an attempt is made to change the {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * of an existing {@link LoadProfileSpec}
     *
     * @param thesaurus       The Thesaurus
     * @param loadProfileSpec the LoadProfileSpec to which the change was attempted
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotChangeDeviceConfigurationReferenceException forLoadProfileSpec(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec) {
        CannotChangeDeviceConfigurationReferenceException exception = new CannotChangeDeviceConfigurationReferenceException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_CHANGE_DEVICE_CONFIG);
        exception.set("loadProfileSpec", loadProfileSpec);
        return exception;
    }
}
