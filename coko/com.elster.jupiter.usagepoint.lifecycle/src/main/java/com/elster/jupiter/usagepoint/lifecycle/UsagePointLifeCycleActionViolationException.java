/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

public abstract class UsagePointLifeCycleActionViolationException extends RuntimeException {

    public UsagePointLifeCycleActionViolationException() {
        super();
    }

    public UsagePointLifeCycleActionViolationException(String message) {
        super(message);
    }

}