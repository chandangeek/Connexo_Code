package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an object from an <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
 *
 * Copyrights EnergyICT
 * Date: 03/02/14
 * Time: 13:29
 */
public class DeviceConfigurationIsActiveException extends LocalizedException {

    /**
     * Creates a new DeviceConfigurationIsActiveException that models the
     * exceptional situation that occurs when an attempt is made
     * to delete a {@link com.energyict.mdc.device.config.RegisterSpec} from an
     * <i>active</i> {@link com.energyict.mdc.device.config.DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The ObisCodeIsRequiredException
     */
    public static DeviceConfigurationIsActiveException canNotDeleteRegisterSpec(Thesaurus thesaurus){
        return new DeviceConfigurationIsActiveException(thesaurus, MessageSeeds.REGISTER_SPEC_PRIME_CHANNEL_SPEC_ALREADY_EXISTS);
    }

    private DeviceConfigurationIsActiveException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }
}
