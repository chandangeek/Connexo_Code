package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when the
 * name of an object is missing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:03)
 */
public class NameIsRequiredException extends LocalizedException {

    public NameIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}