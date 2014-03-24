package com.elster.jupiter.issue.impl.drools;

import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.MessageSeed;

public class DroolsValidationException extends BaseException {

    protected DroolsValidationException(MessageSeed messageSeed, Object... args) {
        super(messageSeed, args);
    }
}
