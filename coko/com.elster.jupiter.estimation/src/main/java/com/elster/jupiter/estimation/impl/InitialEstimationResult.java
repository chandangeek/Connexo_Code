/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

class InitialEstimationResult implements EstimationResult {

    private final List<EstimationBlock> toBeEstimated;

    InitialEstimationResult(List<EstimationBlock> toBeEstimated) {
        this.toBeEstimated = ImmutableList.copyOf(toBeEstimated);
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