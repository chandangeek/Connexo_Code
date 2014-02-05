package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying a {@link com.energyict.mdc.device.config.LoadProfileType}
 * where it is required
 *
 * Copyrights EnergyICT
 * Date: 04/02/14
 * Time: 13:51
 */
public class LoadProfileTypeIsRequiredException extends LocalizedException {

    private LoadProfileTypeIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new LoadProfileTypeIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LoadProfileSpec} without a
     * {@link com.energyict.mdc.device.config.LoadProfileType}
     * @param thesaurus The Thesaurus
     * @return the newly create LoadProfileTypeIsRequiredException
     */
    public static LoadProfileTypeIsRequiredException loadProfileSpecRequiresLoadProfiletype(Thesaurus thesaurus){
        return new LoadProfileTypeIsRequiredException(thesaurus, MessageSeeds.REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED);
    }
}
