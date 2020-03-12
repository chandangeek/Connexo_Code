package com.energyict.mdc.engine.impl.core;

public class FixedQueryTuner implements QueryTuner {
    @Override
    public int getTuningFactor() {
        return 2;
    }

    @Override
    public void calculateFactor(long queryDuration, int jobsSize, int nbConnections) {
    }
}
