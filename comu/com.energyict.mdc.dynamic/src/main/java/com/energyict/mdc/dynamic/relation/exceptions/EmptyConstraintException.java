package com.energyict.mdc.dynamic.relation.exceptions;

import com.energyict.mdc.dynamic.relation.Constraint;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made to create a
 * {@link Constraint} without {@link com.energyict.mdc.dynamic.relation.RelationAttributeType}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class EmptyConstraintException extends LocalizedException {

    public EmptyConstraintException(Thesaurus thesaurus, Constraint constraint, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, constraint.getName());
        this.set("constraintName", constraint.getName());
    }

}