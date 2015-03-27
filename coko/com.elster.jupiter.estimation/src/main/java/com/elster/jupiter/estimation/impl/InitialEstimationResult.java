package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;

import java.util.Collections;
import java.util.List;

public class InitialEstimationResult implements EstimationResult {

    private final List<EstimationBlock> toBeEstimated;

    public InitialEstimationResult(List<EstimationBlock> toBeEstimated) {
        this.toBeEstimated = toBeEstimated;
    }

    @Override
    public List<EstimationBlock> remainingToBeEstimated() {
        return toBeEstimated;
    }

    @Override
    public List<EstimationBlock> estimated() {
        return Collections.emptyList();
    }
}
