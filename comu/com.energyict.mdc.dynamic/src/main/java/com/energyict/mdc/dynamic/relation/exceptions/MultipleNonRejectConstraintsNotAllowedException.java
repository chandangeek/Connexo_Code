package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create multiple {@link com.energyict.mdc.dynamic.relation.Constraint}s
 * that all reject violations.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:46)
 */
public class MultipleNonRejectConstraintsNotAllowedException extends LocalizedException {

    public MultipleNonRejectConstraintsNotAllowedException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}