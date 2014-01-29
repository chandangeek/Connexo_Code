package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.RegisterGroup;

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
     * that occurs when an attempt is made to create a {@link RegisterGroup}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the RegisterGroup that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException registerGroupAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.REGISTER_GROUP_ALREADY_EXISTS, name);
    }

    private DuplicateNameException(Thesaurus thesaurus, MessageSeeds messageSeeds, String name) {
        super(thesaurus, messageSeeds, name);
        this.set("name", name);
    }

}