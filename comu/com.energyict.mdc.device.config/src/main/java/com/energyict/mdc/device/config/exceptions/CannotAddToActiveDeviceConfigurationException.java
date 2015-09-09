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
    public static CannotAddToActiveDeviceConfigurationException aNewLoadProfileSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.ChannelSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewChannelSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.LogBookSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewLogBookSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link com.energyict.mdc.device.config.RegisterSpec}
     * to an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewRegisterSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

}