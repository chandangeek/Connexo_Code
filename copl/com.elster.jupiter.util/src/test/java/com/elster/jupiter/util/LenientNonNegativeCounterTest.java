package com.elster.jupiter.util;

public class LenientNonNegativeCounterTest extends BasicCounterTest {

    @Override
    Counter newCounter() {
        return Counters.newLenientNonNegativeCounter();
    }

    @Override
    boolean allowsDecrements() {
        return true;
    }

    @Override
    boolean allowsNegativeTotals() {
        return false;
    }
}
