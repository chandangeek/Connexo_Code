/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

public interface Destination {
    enum Type {
        FILE,
        DATA,
        COMPOSITE
    }

    Type getType();
}
