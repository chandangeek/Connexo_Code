/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validators.impl.MessageSeeds;

/**
 * Exception indicating misconfiguration detected during main/check validation at {@link com.elster.jupiter.validators.impl.MainCheckValidator}
 */
public class BadMainCheckConfiguration extends LocalizedException {

    public BadMainCheckConfiguration(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }

}
