/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class UnableToCreateIssueException extends LocalizedException {

    private static final long serialVersionUID = -1934348489428630312L;

    public UnableToCreateIssueException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public UnableToCreateIssueException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }
}
