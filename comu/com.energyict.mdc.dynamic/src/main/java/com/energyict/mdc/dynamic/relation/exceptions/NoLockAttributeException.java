package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create a relation type without lock attribute.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class NoLockAttributeException extends LocalizedException {

    public NoLockAttributeException(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED, name);
        this.set("name", name);
    }

}