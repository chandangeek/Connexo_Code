/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.oracle;

import com.elster.jupiter.orm.schema.ExistingSequence;

public class UserSequenceImpl implements ExistingSequence {
    private String name;
    private int minValue;
    private int cacheSize;

    @Override
    public String getName() {
        return name;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    @Override
    public String toString() {
        return "Sequence: " + name;
    }
}
