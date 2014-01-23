package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create an named object but another
 * object of the same type with the same name already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class DuplicateNameException extends LocalizedException {

    public DuplicateNameException(Thesaurus thesaurus, MessageSeed messageSeed, String name) {
        super(thesaurus, messageSeed, name);
        this.set("name", name);
    }

    public DuplicateNameException(Thesaurus thesaurus, MessageSeed messageSeed, String name, String contextName) {
        super(thesaurus, messageSeed, name, contextName);
        this.set("name", name);
        this.set("contextName", contextName);
    }

}