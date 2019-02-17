/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

public final class EvaluableMicroCheckViolation {

    private final ServerMicroCheck microCheck;
    private final String message;

    public EvaluableMicroCheckViolation(ServerMicroCheck microCheck, String message) {
        this.microCheck = microCheck;
        this.message = message;
    }

    public ServerMicroCheck getMicroCheck() {
        return this.microCheck;
    }

    public String getLocalizedMessage() {
        return this.message;
    }
}