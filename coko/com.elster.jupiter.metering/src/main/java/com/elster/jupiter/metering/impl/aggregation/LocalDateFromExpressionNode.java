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
class LocalDateFromExpressionNode implements ServerExpressionNode.Visitor<String> {

    @Override
    public String visitConstant(NumericalConstantNode constant) {
        return null;
    }

    @Override
    public String visitConstant(StringConstantNode constant) {
        return null;
    }

    @Override
    public String visitProperty(CustomPropertyNode property) {
        return null;
    }

    @Override
    public String visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public String visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        return deliverable.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName();
    }

    @Override
    public String visitVirtualRequirement(VirtualRequirementNode requirement) {
        return requirement.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName();
    }

    @Override
    public String visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getExpressionNode().accept(this);
    }

    @Override
    public String visitOperation(OperationNode operationNode) {
        return this.findFirst(Arrays.asList(
                operationNode.getLeftOperand(),
                operationNode.getRightOperand()));
    }

    @Override
    public String visitFunctionCall(FunctionCallNode functionCall) {
        return this.findFirst(functionCall.getArguments());
    }

    private String findFirst(List<ServerExpressionNode> children) {
        return children
                .stream()
                .map(child -> child.accept(this))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

}