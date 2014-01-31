package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.device.config.RegisterMapping}.
 *
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 13:38
 */
public class RegisterMappingIsRequiredException extends LocalizedException {

    private RegisterMappingIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
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
    public static RegisterMappingIsRequiredException registerSpecRequiresRegisterMapping(Thesaurus thesaurus){
        return new RegisterMappingIsRequiredException(thesaurus, MessageSeeds.REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED);
    }
}
