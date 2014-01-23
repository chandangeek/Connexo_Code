package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to create a relation type with a lock attribute
 * that is <strong>NOT</strong> a reference to another object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class LockAttributeShouldBeReferenceTypeException extends LocalizedException {

    public LockAttributeShouldBeReferenceTypeException(Thesaurus thesaurus, String relationTypeName, String lockAttributeName) {
        super(thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED, relationTypeName, lockAttributeName);
        this.set("relationTypeName", relationTypeName);
        this.set("lockAttributeName", lockAttributeName);
    }

}