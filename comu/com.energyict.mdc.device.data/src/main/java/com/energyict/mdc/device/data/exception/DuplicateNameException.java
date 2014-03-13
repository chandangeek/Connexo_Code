package com.energyict.mdc.device.data.exception;

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

    private DuplicateNameException(Thesaurus thesaurus, MessageSeeds messageSeeds, String name) {
        super(thesaurus, messageSeeds, name);
        this.set("name", name);
    }

    public static DuplicateNameException deviceWithExternalNameExists(Thesaurus thesaurus, String externalName) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_DEVICE_EXTERNAL_NAME, externalName);
    }

}