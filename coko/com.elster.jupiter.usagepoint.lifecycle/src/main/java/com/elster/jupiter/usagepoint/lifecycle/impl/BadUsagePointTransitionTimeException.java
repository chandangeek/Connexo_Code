package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class BadUsagePointTransitionTimeException extends LocalizedException {
    protected BadUsagePointTransitionTimeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}
