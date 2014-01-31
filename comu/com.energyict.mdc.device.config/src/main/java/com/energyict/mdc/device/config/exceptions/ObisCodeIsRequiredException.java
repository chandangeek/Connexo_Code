package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.common.ObisCode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (11:11)
 */
public class ObisCodeIsRequiredException extends LocalizedException {

    /**
     * Creates a new ObisCodeIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made
     * to create a {@link com.energyict.mdc.device.config.RegisterMapping}
     * without an {@link com.energyict.mdc.common.ObisCode}.
     *
     * @param thesaurus The Thesaurus
     * @return The ObisCodeIsRequiredException
     */
    public static ObisCodeIsRequiredException registerMappingRequiresObisCode (Thesaurus thesaurus) {
        return new ObisCodeIsRequiredException(thesaurus, MessageSeeds.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED);
    }

    /**
     * Creates a new ObisCodeIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made
     * to create a {@link com.energyict.mdc.device.config.LoadProfileType}
     * without an {@link com.energyict.mdc.common.ObisCode}.
     *
     * @param thesaurus The Thesaurus
     * @return The ObisCodeIsRequiredException
     */
    public static ObisCodeIsRequiredException loadProfileTypeRequiresObisCode (Thesaurus thesaurus) {
        return new ObisCodeIsRequiredException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED);
    }

    /**
     * Creates a new ObisCodeIsRequiredException that models the
     * exceptional situation that occurs when an attempt is made
     * to create a {@link com.energyict.mdc.device.config.LogBookType}
     * without an {@link com.energyict.mdc.common.ObisCode}.
     *
     * @param thesaurus The Thesaurus
     * @return The ObisCodeIsRequiredException
     */
    public static ObisCodeIsRequiredException logBookTypeRequiresObisCode (Thesaurus thesaurus) {
        return new ObisCodeIsRequiredException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED);
    }

    private ObisCodeIsRequiredException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }

}