package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an object from an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 08:37
 */
public class CannotDeleteFromActiveDeviceConfigurationException extends LocalizedException {

    private CannotDeleteFromActiveDeviceConfigurationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus           the Thesaurus
     * @param loadProfileSpec     the LoadProfileSpec that you tried to delete
     * @param deviceConfiguration the active DeviceConfiguration
     * @return the newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forLoadProfileSpec(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec, DeviceConfiguration deviceConfiguration) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
        cannotDeleteFromActiveDeviceConfigurationException.set("loadProfileSpec", loadProfileSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus           the Thesaurus
     * @param logBookSpec         the LogBookSpec that you tried to delete
     * @param deviceConfiguration the active DeviceConfiguration
     * @return the newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forLogbookSpec(Thesaurus thesaurus, LogBookSpec logBookSpec, DeviceConfiguration deviceConfiguration) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, MessageSeeds.LOGBOOK_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
        cannotDeleteFromActiveDeviceConfigurationException.set("logBookSpec", logBookSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link ChannelSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus           the Thesaurus
     * @param channelSpec         the ChannelSpec that you tried to delete
     * @param deviceConfiguration the active DeviceConfiguration
     * @return the newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec, DeviceConfiguration deviceConfiguration) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, MessageSeeds.CHANNEL_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
        cannotDeleteFromActiveDeviceConfigurationException.set("channelSpec", channelSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the
     * exceptional situation that occurs when an attempt is made
     * to delete a {@link com.energyict.mdc.device.config.RegisterSpec} from an
     * <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus           The Thesaurus
     * @param deviceConfiguration
     * @param registerSpec
     * @return The CannotDeleteFromActiveDeviceConfigurationException
     */
    public static CannotDeleteFromActiveDeviceConfigurationException canNotDeleteRegisterSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, RegisterSpec registerSpec) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, MessageSeeds.REGISTER_SPEC_CANNOT_DELETE_FOR_ACTIVE_CONFIG);
        cannotDeleteFromActiveDeviceConfigurationException.set("registerSpec", registerSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

}
