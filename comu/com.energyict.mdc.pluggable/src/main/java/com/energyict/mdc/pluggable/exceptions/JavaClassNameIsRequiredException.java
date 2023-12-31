/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.pluggable.PluggableClass;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new {@link PluggableClass} without a name.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (15:30)
 */
public class JavaClassNameIsRequiredException extends LocalizedException {

    public JavaClassNameIsRequiredException(MessageSeed messageSeed, Thesaurus thesaurus, String pluggableClassName) {
        super(thesaurus, messageSeed, pluggableClassName);
        this.set("pluggableClassName", pluggableClassName);
    }

}