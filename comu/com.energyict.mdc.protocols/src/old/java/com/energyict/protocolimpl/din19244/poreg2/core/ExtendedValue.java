/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.core;

import java.io.IOException;

public class ExtendedValue {

    private int value;
    private DataType type;
    private boolean valid;

    public ExtendedValue(DataType type, int value) {
        this.type = type;
        this.value = value;
        this.valid = true;
    }

    public ExtendedValue(DataType type, int value, boolean valid) {
        this.type = type;
        this.value = value;
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public DataType getType() {
        return type;
    }

    public int getValue() throws IOException {
        if (!valid) {
            throw new IOException("Error requesting data, returned value with length 0 (missing)");
        }
        return value;
    }
}
