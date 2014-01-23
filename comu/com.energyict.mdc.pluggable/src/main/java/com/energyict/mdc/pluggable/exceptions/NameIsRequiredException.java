package com.energyict.mdc.pluggable.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.pluggable.PluggableClass;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new {@link PluggableClass} without a name.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (15:30)
 */
public class NameIsRequiredException extends LocalizedException {

    public NameIsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.NAME_IS_REQUIRED);
    }

}