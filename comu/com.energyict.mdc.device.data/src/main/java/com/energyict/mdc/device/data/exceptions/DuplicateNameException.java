/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create an entity of this bundle
 * but another one of the same type and name already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-11 (13:19)
 */
public class DuplicateNameException extends LocalizedException {

    protected DuplicateNameException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}