package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class InvalidQueryGroupException extends LocalizedException {

    public InvalidQueryGroupException(Thesaurus thesaurus, MessageSeeds messageSeed, Throwable throwable, Object... args) {
        super(thesaurus, messageSeed, throwable, args);
    }

    public InvalidQueryGroupException(Thesaurus thesaurus, MessageSeeds messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}
