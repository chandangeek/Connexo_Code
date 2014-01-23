package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when the
 * name of an object contains invalid characters.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class NameContainsInvalidCharactersException extends LocalizedException {

    public NameContainsInvalidCharactersException(Thesaurus thesaurus, MessageSeed messageSeed, String name, String validCharacters) {
        super(thesaurus, messageSeed, name, validCharacters);
        this.set("name", name);
        this.set("validCharacters", validCharacters);
    }

}