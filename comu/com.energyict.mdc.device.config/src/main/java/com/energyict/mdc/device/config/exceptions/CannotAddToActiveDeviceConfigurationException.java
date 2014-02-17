package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add something to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 04/02/14
 * Time: 16:06
 */
public class CannotAddToActiveDeviceConfigurationException extends LocalizedException {

    private CannotAddToActiveDeviceConfigurationException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.LoadProfileSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewLoadProfileSpec(Thesaurus thesaurus) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.ChannelSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewChannelSpec(Thesaurus thesaurus) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, MessageSeeds.CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.LogBookSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewLogBookSpec(Thesaurus thesaurus) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, MessageSeeds.LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION);
    }
}
