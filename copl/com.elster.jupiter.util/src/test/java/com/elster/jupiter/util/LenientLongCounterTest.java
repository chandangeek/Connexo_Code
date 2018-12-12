/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public class LenientLongCounterTest extends BasicLongCounterTest {

    @Override
    LongCounter newLongCounter() {
        return Counters.newLenientLongCounter();
    }

    @Override
    boolean allowsDecrements() {
        return true;
    }

    @Override
    boolean allowsNegativeTotals() {
        return true;
    }
}
