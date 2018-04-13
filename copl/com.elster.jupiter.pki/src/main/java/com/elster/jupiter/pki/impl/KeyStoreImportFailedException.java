/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Throw whenever the import of a KeyStore failed. A more specific message is added as well.
 */
public class KeyStoreImportFailedException extends LocalizedException {
    protected KeyStoreImportFailedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

    protected KeyStoreImportFailedException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }
}
