package com.energyict.mdc.common;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;

/**
 * This is the new base class of Jupiter server side exceptions
 * Application exception that facilitates translation by means of externalized strings and Java properties
 */
public class TranslatableApplicationException extends LocalizedException implements TranslatableException {

    @Inject
    public TranslatableApplicationException(Thesaurus thesaurus, @Assisted MessageSeed messageSeed, @Assisted Object ...args) {
        super(thesaurus, messageSeed, args);
    }
}
