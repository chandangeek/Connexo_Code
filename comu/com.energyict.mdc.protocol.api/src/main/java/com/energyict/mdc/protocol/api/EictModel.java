/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

/**
 * Placeholder implementation
 */
public enum EictModel implements Model {
    AM100,
    ABBA1500;

    @Override
    public String getName() {
        return this.name();
    }
}
