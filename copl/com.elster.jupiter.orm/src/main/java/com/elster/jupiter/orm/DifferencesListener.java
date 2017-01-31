/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
@FunctionalInterface
public interface DifferencesListener {
    void onDifference(Difference difference);

    default void done() {
        // do nothing be default
    }
}
