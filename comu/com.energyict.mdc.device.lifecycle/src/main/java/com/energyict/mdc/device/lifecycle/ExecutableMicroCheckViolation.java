/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

public final class ExecutableMicroCheckViolation {

    private final ExecutableMicroCheck microCheck;
    private final String message;

    public ExecutableMicroCheckViolation(ExecutableMicroCheck microCheck, String message) {
        this.microCheck = microCheck;
        this.message = message;
    }

    public ExecutableMicroCheck getCheck() {
        return this.microCheck;
    }

    public String getLocalizedMessage() {
        return this.message;
    }
}
