package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.impl;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition}
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