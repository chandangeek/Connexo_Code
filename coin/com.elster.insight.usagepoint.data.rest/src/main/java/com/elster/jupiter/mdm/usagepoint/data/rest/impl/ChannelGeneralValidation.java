/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-06 (13:21)
 */
public class ChannelGeneralValidation {
    private boolean isValidationActive;
    private Instant lastChecked;

    public ChannelGeneralValidation(boolean isValidationActive, Instant lastChecked) {
        this.isValidationActive = isValidationActive;
        this.lastChecked = lastChecked;
    }

    public boolean isValidationActive() {
        return isValidationActive;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }
}