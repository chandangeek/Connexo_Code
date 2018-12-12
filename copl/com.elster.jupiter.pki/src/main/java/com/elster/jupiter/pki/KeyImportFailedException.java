/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Throw whenever the import of a Key failed. A more specific message is added as well.
 */
public class KeyImportFailedException extends LocalizedException {
    public KeyImportFailedException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

    public KeyImportFailedException(Thesaurus thesaurus, MessageSeed messageSeed, Object ... objects) {
        super(thesaurus, messageSeed, objects);
    }
}
