package com.energyict.mdc.engine.impl.core;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

public class AdaptiveQueryTunerTest {

    @Test
    public void getDefaultFactor() {
        AdaptiveQueryTuner tuner = new AdaptiveQueryTuner();
        assertEquals(tuner.getTuningFactor(), 2);
    }

    @Test
    public void getNewFactor() {
        AdaptiveQueryTuner tuner = new AdaptiveQueryTuner();
        tuner.calculateFactor(2500, 164, 650);
        assertEquals(tuner.getTuningFactor(), 3);
        tuner.calculateFactor(4800, 328, 650);
        assertEquals(tuner.getTuningFactor(), 4);
        tuner.calculateFactor(6800, 492, 650);
        assertEquals(tuner.getTuningFactor(), 5);
        tuner.calculateFactor(10800, 700, 650);
        assertEquals(tuner.getTuningFactor(), 4);
    }
}
