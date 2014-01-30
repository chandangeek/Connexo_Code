package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without specifying a name for it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:00)
 */
public class NameIsRequiredException extends LocalizedException {

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterGroup} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException registerGroupNameIsRequired (Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.REGISTER_GROUP_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterMapping} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException registerMappingNameIsRequired (Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.REGISTER_MAPPING_NAME_IS_REQUIRED);
    }

    private NameIsRequiredException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }

}