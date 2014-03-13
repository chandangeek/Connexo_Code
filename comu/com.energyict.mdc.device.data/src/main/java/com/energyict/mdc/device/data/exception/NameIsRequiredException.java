package com.energyict.mdc.device.data.exception;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 03/03/14
 * Time: 16:00
 */
public class NameIsRequiredException extends LocalizedException {

    private NameIsRequiredException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterGroup} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException deviceNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.DEVICE_NAME_IS_REQUIRED);
    }


}