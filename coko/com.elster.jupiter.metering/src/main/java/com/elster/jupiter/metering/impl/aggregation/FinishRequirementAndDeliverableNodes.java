/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that calls {@link VirtualRequirementNode#finish()}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-04 (15:40)
 */
class FinishRequirementAndDeliverableNodes implements ServerExpressionNode.Visitor<Void> {
    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        requirement.finish();
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        return null;
    }

    @Override
    public Void visitConstant(NumericalConstantNode constant) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitConstant(StringConstantNode constant) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitSqlFragment(SqlFragmentNode variable) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
        // Nothing to finish here
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operation) {
        operation.getLeftOperand().accept(this);
        operation.getRightOperand().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getArguments().forEach(child -> child.accept(this));
        return null;
    }

    @Override
    public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

}