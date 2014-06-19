package com.elster.jupiter.issue.datacollection.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UnableToCreateEventException extends LocalizedException {
    
    private static final long serialVersionUID = -1934348489428630312L;

    public UnableToCreateEventException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }
}
