/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by bvn on 2/15/17.
 */
public class DuplicateKeyEncryptionRegistration extends LocalizedException {
    protected DuplicateKeyEncryptionRegistration(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DUPLICATE_KEY_ENCRYPTION_REGISTRATION);
    }
}
