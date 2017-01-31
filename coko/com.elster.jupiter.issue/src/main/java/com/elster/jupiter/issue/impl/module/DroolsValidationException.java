/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class DroolsValidationException extends LocalizedException {

    private static final long serialVersionUID = 1L;

    public DroolsValidationException(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.ISSUE_DROOLS_VALIDATION, args);
    }
}
