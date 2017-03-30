/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor}
 * interface and returns a SQL construct (as String) that provides a LOCALDATE for the visited
 * {@link ExpressionNode}
 * or <code>null</code> if the ExpressionNode cannot provide such a LOCALDATE.
 * A {@link NumericalConstantNode} is a good example of that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
class IntervalLengthFromExpressionNode implements ServerExpressionNode.Visitor<IntervalLength> {

    @Override
    public IntervalLength visitConstant(NumericalConstantNode constant) {
        return null;
    }

    @Override
    public IntervalLength visitConstant(StringConstantNode constant) {
        return null;
    }

    @Override
    public IntervalLength visitProperty(CustomPropertyNode property) {
        return null;
    }

    @Override
    public IntervalLength visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return slp.getIntervalLength();
    }

    @Override
    public IntervalLength visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public IntervalLength visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public IntervalLength visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        return null;
    }

    @Override
    public IntervalLength visitVirtualRequirement(VirtualRequirementNode requirement) {
        return null;
    }

    @Override
    public IntervalLength visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getExpressionNode().accept(this);
    }

    @Override
    public IntervalLength visitOperation(OperationNode operationNode) {
        return this.findFirst(Arrays.asList(
                operationNode.getLeftOperand(),
                operationNode.getRightOperand()));
    }

    @Override
    public IntervalLength visitFunctionCall(FunctionCallNode functionCall) {
        return this.findFirst(functionCall.getArguments());
    }

    private IntervalLength findFirst(List<ServerExpressionNode> children) {
        return children
                .stream()
                .map(child -> child.accept(this))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public IntervalLength visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getIntervalLength();
    }

}