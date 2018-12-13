/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public class LenientThreadSafeCounterTest extends BasicCounterTest {

    @Override
    Counter newCounter() {
        return Counters.newLenientThreadSafeCounter();
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
