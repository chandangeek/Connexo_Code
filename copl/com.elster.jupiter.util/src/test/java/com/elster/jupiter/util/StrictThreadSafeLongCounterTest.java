/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public class StrictThreadSafeLongCounterTest extends BasicLongCounterTest {

    @Override
    LongCounter newLongCounter() {
        return Counters.newStrictThreadSafeLongCounter();
    }

    @Override
    boolean allowsDecrements() {
        return false;
    }

    @Override
    boolean allowsNegativeTotals() {
        return false;
    }
}