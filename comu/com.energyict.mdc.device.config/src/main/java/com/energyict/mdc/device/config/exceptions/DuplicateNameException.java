package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create an entity of this bundle
 * but another one of the same type and name already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:10)
 */
public class DuplicateNameException extends LocalizedException {

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.RegisterGroup}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the RegisterGroup that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException registerGroupAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.REGISTER_GROUP_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.RegisterMapping}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the RegisterMapping that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException registerMappingAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.REGISTER_MAPPING_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.LoadProfileType}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LoadProfileType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException loadProfileTypeAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.LogBookType}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LogBookType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException logBookTypeAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_ALREADY_EXISTS, name);
    }

    private DuplicateNameException(Thesaurus thesaurus, MessageSeeds messageSeeds, String name) {
        super(thesaurus, messageSeeds, name);
        this.set("name", name);
    }

}