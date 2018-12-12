/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationResultBuilder;

import com.google.common.collect.ImmutableList;

import java.util.List;

class SimpleEstimationResult implements EstimationResult {

    private final ImmutableList<EstimationBlock> remain;
    private final ImmutableList<EstimationBlock> estimated;

    private SimpleEstimationResult(ImmutableList<EstimationBlock> remain, ImmutableList<EstimationBlock> estimated) {
        this.remain = remain;
        this.estimated = estimated;
    }

    public static SimpleEstimationResult of(List<EstimationBlock> remain, List<EstimationBlock> estimated) {
        return new SimpleEstimationResult(ImmutableList.copyOf(remain), ImmutableList.copyOf(estimated));
    }

    @Override
    public List<EstimationBlock> remainingToBeEstimated() {
        return remain;
    }

    @Override
    public List<EstimationBlock> estimated() {
        return estimated;
    }

    public static EstimationResultBuilder builder() {
        return new EstimationResultBuilderImpl();
    }

    static final class EstimationResultBuilderImpl implements EstimationResultBuilder {
        private ImmutableList.Builder<EstimationBlock> remain = ImmutableList.builder();
        private ImmutableList.Builder<EstimationBlock> estimated = ImmutableList.builder();

        private EstimationResultBuilderImpl() {
        }

        @Override
        public void addRemaining(EstimationBlock block) {
            remain.add(block);
        }

        @Override
        public void addEstimated(EstimationBlock block) {
            estimated.add(block);
        }

        @Override
        public EstimationResult build() {
            return new SimpleEstimationResult(remain.build(), estimated.build());
        }
    }

}