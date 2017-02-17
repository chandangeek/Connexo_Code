/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * This is the exception when any of the KeyException, Algorithm-exceptions, ... are thrown
 */
public class PkiLocalException extends LocalizedException {
    public PkiLocalException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable throwable) {
        super(thesaurus, messageSeed, throwable);
    }
}
