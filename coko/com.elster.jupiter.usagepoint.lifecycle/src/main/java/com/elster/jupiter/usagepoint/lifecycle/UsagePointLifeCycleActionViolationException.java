package com.elster.jupiter.usagepoint.lifecycle;

public abstract class UsagePointLifeCycleActionViolationException extends RuntimeException {

    public UsagePointLifeCycleActionViolationException() {
        super();
    }

    public UsagePointLifeCycleActionViolationException(String message) {
        super(message);
    }

}