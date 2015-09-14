package com.energyict.mdc.pluggable.exceptions;

import com.energyict.mdc.pluggable.PluggableClass;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new {@link PluggableClass} without a name.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (15:30)
 */
public class NameIsRequiredException extends LocalizedException {

    public NameIsRequiredException(MessageSeed messageSeed, Thesaurus thesaurus) {
        super(thesaurus, messageSeed);
    }

}