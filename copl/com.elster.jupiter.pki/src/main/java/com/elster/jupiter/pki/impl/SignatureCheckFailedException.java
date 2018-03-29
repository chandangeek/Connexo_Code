/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class SignatureCheckFailedException extends LocalizedException {
    public SignatureCheckFailedException(Thesaurus thesaurus) {
        this(thesaurus, MessageSeeds.BAD_SIGNATURE);
    }

    public SignatureCheckFailedException(Thesaurus thesaurus, Throwable throwable) {
        super(thesaurus, MessageSeeds.BAD_SIGNATURE, throwable);
    }

    public SignatureCheckFailedException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}
