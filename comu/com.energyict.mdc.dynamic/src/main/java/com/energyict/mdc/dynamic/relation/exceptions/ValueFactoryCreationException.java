package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a value factory
 * could not be created. This is a wrapper for all of the
 * java reflection layer exceptions that may occur when
 * a new instance of a class is created.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (12:56)
 */
public class ValueFactoryCreationException extends LocalizedException {

    public ValueFactoryCreationException(Thesaurus thesaurus, Throwable cause, String valueFactoryClassName) {
        super(thesaurus, MessageSeeds.VALUEFACTORY_CREATION, cause, valueFactoryClassName);
    }

}