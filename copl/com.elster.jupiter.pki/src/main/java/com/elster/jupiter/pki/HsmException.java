/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Throw whenever an interaction with the HSM has failed
 */
public class HsmException extends LocalizedException {
    protected HsmException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    protected HsmException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    protected HsmException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

    protected HsmException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }
}
