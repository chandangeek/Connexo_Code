/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public final class IssueActionClassLoadFailedException extends LocalizedException {

    private static final long serialVersionUID = 1;

    public IssueActionClassLoadFailedException(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.ISSUE_ACTION_CLASS_LOAD_FAIL, args);
    }
}
