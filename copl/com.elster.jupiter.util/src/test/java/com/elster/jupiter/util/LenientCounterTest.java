/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public class LenientCounterTest extends BasicCounterTest {

    @Override
    Counter newCounter() {
        return Counters.newLenientCounter();
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
