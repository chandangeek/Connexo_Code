/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

public enum DerivationRule {
    MEASURED,
    MULTIPLIED,
    DELTA,
    MULTIPLIED_DELTA;

    public boolean isDelta() {
        switch (this) {
            case DELTA:
            case MULTIPLIED_DELTA:
                return true;
            default:
                return false;
        }
    }

    public boolean isMultiplied() {
        switch (this) {
            case MULTIPLIED:
            case MULTIPLIED_DELTA:
                return true;
            default:
                return false;
        }
    }
}
