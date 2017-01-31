/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a pluggable class
 * could not be created. This is a wrapper for all of the
 * java reflection layer exceptions that may occur when
 * a new instance of a class is created.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (10:11)
 */
public class PluggableClassCreationException extends LocalizedException {

    public PluggableClassCreationException(Thesaurus thesaurus, String javaClassName, Throwable cause) {
        super(thesaurus, MessageSeeds.PLUGGABLE_CLASS_CREATION_FAILURE, cause, javaClassName);
        this.set("javaClassName", javaClassName);
    }

}