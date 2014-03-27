package com.elster.jupiter.issue.impl.drools;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.exception.MessageSeed;

public class DroolsValidationException extends LocalizedException {

    protected DroolsValidationException(MessageSeed messageSeed, Object... args) {
        super(null, messageSeed, args);
    }
}
