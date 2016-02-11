package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Redefines a {@link ReadingTypeDeliverable} for a {@link MeterActivation}.
 * Maintains a copy of the original expression tree because the target
 * intervals of the nodes that reference e.g. a Channel may be different
 * depending on the actual reading types of those Channels.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (09:46)
 */
class ReadingTypeDeliverableForMeterActivation {

    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private final Range<Instant> requestedPeriod;
    private final int meterActivationSequenceNumber;
    private final ServerExpressionNode expressionNode;
    private final IntervalLength expressionAggregationInterval;

    ReadingTypeDeliverableForMeterActivation(ReadingTypeDeliverable deliverable, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber, ServerExpressionNode expressionNode, IntervalLength expressionAggregationInterval) {
        super();
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.requestedPeriod = requestedPeriod;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
        this.expressionNode = expressionNode;
        this.expressionAggregationInterval = expressionAggregationInterval;
    }

    ReadingTypeDeliverable getDeliverable() {
        return this.deliverable;
    }

    ReadingType getReadingType () {
        return this.deliverable.getReadingType();
    }

    void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.expressionNode.accept(new FinishRequirementAndDeliverableNodes());
        // Todo: add select or with clause, still need to figure out which one we need
    }

    private class FinishRequirementAndDeliverableNodes implements ServerExpressionNode.ServerVisitor<Void> {
        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.finish();
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.finish();
            return null;
        }

        @Override
        public Void visitConstant(ConstantNode constant) {
            // Nothing to finish here
            return null;
        }

        @Override
        public Void visitRequirement(ReadingTypeRequirementNode requirement) {
            throw new IllegalArgumentException("ReadingTypeRequirement nodes should have been replaced with virtual ones");
        }

        @Override
        public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            throw new IllegalArgumentException("ReadingTypeDeliverable nodes should have been replaced with virtual ones");
        }

        @Override
        public Void visitOperation(OperationNode operation) {
            operation.getLeftOperand().accept(this);
            operation.getRightOperand().accept(this);
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            functionCall.getChildren().forEach(child -> child.accept(this));
            return null;
        }
    }

}