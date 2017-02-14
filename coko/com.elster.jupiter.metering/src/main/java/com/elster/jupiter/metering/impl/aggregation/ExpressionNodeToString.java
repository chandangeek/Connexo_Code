/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor}
 * interface that generates SQL as a String for the visited {@link ExpressionNode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (14:36)
 */
class ExpressionNodeToString implements ServerExpressionNode.Visitor<String> {

    private final Formula.Mode mode;
    private boolean unitConversionActive = false;

    ExpressionNodeToString(Formula.Mode mode) {
        this.mode = mode;
    }

    @Override
    public String visitConstant(NumericalConstantNode constant) {
        return String.valueOf(constant.getValue());
    }

    @Override
    public String visitNull(NullNode nullNode) {
        return "null";
    }

    @Override
    public String visitConstant(StringConstantNode constant) {
        return constant.getValue();
    }

    @Override
    public String visitProperty(CustomPropertyNode property) {
        return property.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName();
    }

    @Override
    public String visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return this.visitProperty(slp);
    }

    @Override
    public String visitSqlFragment(SqlFragmentNode variable) {
        return variable.getSqlFragment().getText();
    }

    @Override
    public String visitUnitConversion(UnitConversionNode unitConversionNode) {
        VirtualReadingType sourceReadingType = unitConversionNode.getSourceReadingType();
        this.unitConversionActive = true;
        String expression = unitConversionNode.getExpressionNode().accept(this);
        this.unitConversionActive = false;
        return sourceReadingType.buildSqlUnitConversion(this.mode, expression, unitConversionNode.getTargetReadingType());
    }

    @Override
    public String visitOperation(OperationNode operationNode) {
        StringBuilder fragment = new StringBuilder("(");
        if (Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            operationNode
                    .getOperator()
                    .appendTo(
                            fragment,
                            operationNode.getLeftOperand().accept(this),
                            operationNode.getRightOperand().accept(this),
                            operationNode.getSafeDivisor().accept(this));
        } else {
            operationNode
                    .getOperator()
                    .appendTo(
                            fragment,
                            operationNode.getLeftOperand().accept(this),
                            operationNode.getRightOperand().accept(this),
                            null);
        }
        fragment.append(")");
        return fragment.toString();
    }

    @Override
    public String visitFunctionCall(FunctionCallNode functionCall) {
        List<String> arguments = functionCall.getArguments().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        StringBuilder fragment = new StringBuilder();
        Function function = functionCall.getFunction();
        function.appendTo(fragment, arguments);
        return fragment.toString();
    }

    @Override
    public String visitVirtualRequirement(VirtualRequirementNode requirement) {
        SqlBuilder fragment = new SqlBuilder();
        if (this.unitConversionActive) {
            requirement.appendTo(fragment);
        } else {
            requirement.appendToWithUnitConversion(fragment);
        }
        return fragment.getText();
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        SqlBuilder fragment = new SqlBuilder();
        if (this.unitConversionActive) {
            deliverable.appendTo(fragment);
        } else {
            deliverable.appendToWithUnitConversion(fragment);
        }
        return fragment.getText();
    }

    @Override
    public String visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        StringBuilder fragment = new StringBuilder();
        aggregationNode
                .getFunction()
                .appendTo(
                        fragment,
                        Collections.singletonList(aggregationNode.getAggregatedExpression().accept(this)));
        return fragment.toString();
    }

}