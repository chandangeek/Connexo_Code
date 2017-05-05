/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

class SkipValidationException extends LocalizedException {

    private final SkipValidationOption skipValidationOption;

    SkipValidationException(Thesaurus thesaurus, SkipValidationOption skipValidationOption, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
        this.skipValidationOption = skipValidationOption;
    }

    SkipValidationOption getSkipValidationOption() {
        return skipValidationOption;
    }
}
