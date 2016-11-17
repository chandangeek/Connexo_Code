package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

/**
 * Models the exceptional situation that occurs when an {@link UsagePointTransition}
 * is executed by the user but failed due to some business constraint violations.
 */
public abstract class UsagePointLifeCycleViolationException extends RuntimeException {

    public UsagePointLifeCycleViolationException() {
        super();
    }

    public UsagePointLifeCycleViolationException(String message) {
        super(message);
    }

}