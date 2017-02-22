/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * This is the exception when any of the KeyException, Algorithm-exceptions, ... checked exceptions are thrown.
 * Checked exceptions are wrapped using this runtime exception.
 */
public class PkiLocalizedException extends LocalizedException {

    public PkiLocalizedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable throwable) {
        super(thesaurus, messageSeed, throwable);
    }
}
