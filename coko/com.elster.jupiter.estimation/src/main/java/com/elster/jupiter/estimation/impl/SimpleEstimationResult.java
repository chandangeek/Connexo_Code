package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
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
        return new EstimationResultBuilder();
    }

    public static class EstimationResultBuilder {
        private ImmutableList.Builder<EstimationBlock> remain = ImmutableList.builder();
        private ImmutableList.Builder<EstimationBlock> estimated = ImmutableList.builder();

        private EstimationResultBuilder() {
        }

        public void addRemaining(EstimationBlock block) {
            remain.add(block);
        }

        public void addEstimated(EstimationBlock block) {
            remain.add(block);
        }

        public void addRemaining(EstimationBlock... block) {
            addRemaining(Arrays.asList(block));
        }

        public void addEstimated(EstimationBlock... block) {
            addEstimated(Arrays.asList(block));
        }

        public void addRemaining(Iterable<? extends EstimationBlock> blocks) {
            remain.addAll(blocks);
        }

        public void addEstimated(Iterable<? extends EstimationBlock> blocks) {
            estimated.addAll(blocks);
        }

        public EstimationResult build() {
            return new SimpleEstimationResult(remain.build(), estimated.build());
        }
    }
}
