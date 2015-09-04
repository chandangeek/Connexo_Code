package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create a relation type without lock attribute.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class NoLockAttributeException extends LocalizedException {

    public NoLockAttributeException(String name, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, name);
        this.set("name", name);
    }

}