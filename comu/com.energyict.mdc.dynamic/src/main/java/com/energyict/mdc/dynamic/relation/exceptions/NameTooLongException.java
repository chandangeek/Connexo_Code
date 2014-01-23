package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when the
 * name of an object is too long.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class NameTooLongException extends LocalizedException {

    public NameTooLongException(Thesaurus thesaurus, MessageSeed messageSeed, String name, int maximumLength) {
        super(thesaurus, messageSeed, name, maximumLength);
        this.set("name", name);
        this.set("maximumLength", maximumLength);
    }

}