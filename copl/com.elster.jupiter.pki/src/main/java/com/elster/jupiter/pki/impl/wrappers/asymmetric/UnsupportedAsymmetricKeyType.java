/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;

/**
 * Created by bvn on 3/28/17.
 */
public class UnsupportedAsymmetricKeyType extends LocalizedException {
    public UnsupportedAsymmetricKeyType(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.UNSUPPORTED_KEY_TYPE, args);
    }
}
