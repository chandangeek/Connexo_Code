package com.energyict.mdc.engine.impl.core;

public interface QueryTuner {

    int getTuningFactor();

    void calculateFactor(long queryDuration, int jobsSize, int nbConnections);
}
