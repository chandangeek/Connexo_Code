/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Created by igh on 15/10/2015.
 */
public class DestinationFailedException extends LocalizedException {

    public DestinationFailedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... argse) {
        super(thesaurus, messageSeed, cause, argse);
    }

}
