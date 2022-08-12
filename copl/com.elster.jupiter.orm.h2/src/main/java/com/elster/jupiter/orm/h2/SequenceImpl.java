/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2;

import com.elster.jupiter.orm.schema.ExistingSequence;

public class SequenceImpl implements ExistingSequence {
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Sequence: " + name;
    }
}
