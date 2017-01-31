/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

/**
 * Models a violation of one of the {@link MicroCheck}s
 * that are configured on an {@link UsagePointTransition}.
 */
public final class ExecutableMicroCheckViolation {

    private final ExecutableMicroCheck microCheck;
    private final String message;

    public ExecutableMicroCheckViolation(ExecutableMicroCheck microCheck, String message) {
        this.microCheck = microCheck;
        this.message = message;
    }

    public ExecutableMicroCheck getMicroCheck() {
        return this.microCheck;
    }

    public String getLocalizedMessage() {
        return this.message;
    }
}