package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

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
     * @return
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forLoadProfileSpec(Thesaurus thesaurus, LoadProfileSpec loadProfileSpec, DeviceConfiguration deviceConfiguration) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG, loadProfileSpec, deviceConfiguration);
        cannotDeleteFromActiveDeviceConfigurationException.set("loadProfileSpec", loadProfileSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }
}
