/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public class StrictCounterTest extends BasicCounterTest {

    @Override
    Counter newCounter() {
        return Counters.newStrictCounter();
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