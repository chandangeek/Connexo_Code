package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete the default {@link com.energyict.mdc.dynamic.relation.Constraint}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class CannotDeleteDefaultRelationConstraintException extends LocalizedException {

    public CannotDeleteDefaultRelationConstraintException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CONSTRAINT_CANNOT_DELETE_DEFAULT);
    }

}