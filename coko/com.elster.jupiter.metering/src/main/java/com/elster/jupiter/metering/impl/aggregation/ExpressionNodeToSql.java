/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link com.elster.jupiter.metering.impl.aggregation.ServerExpressionNode.Visitor}
 * interface that generates SQL for the visited {@link ExpressionNode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (12:41)
 */
class ExpressionNodeToSql implements ServerExpressionNode.Visitor<SqlFragment> {

    private final Formula.Mode mode;
    private boolean unitConversionActive = false;

    ExpressionNodeToSql(Formula.Mode mode) {
        this.mode = mode;
    }

    @Override
    public SqlFragment visitConstant(NumericalConstantNode constant) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.addObject(constant.getValue());
        return fragment;
    }

    @Override
    public SqlFragment visitNull(NullNode nullNode) {
        return new SqlBuilder("null");
    }

    @Override
    public SqlFragment visitConstant(StringConstantNode constant) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.addObject(constant.getValue());
        return fragment;
    }

    @Override
    public SqlFragment visitProperty(CustomPropertyNode property) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.append(property.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
        return fragment;
    }

    @Override
    public SqlFragment visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return this.visitProperty(slp);
    }

    @Override
    public SqlFragment visitSqlFragment(SqlFragmentNode variable) {
        return variable.getSqlFragment();
    }

    @Override
    public SqlFragment visitUnitConversion(UnitConversionNode unitConversionNode) {
        VirtualReadingType sourceReadingType = unitConversionNode.getSourceReadingType();
        VirtualReadingType targetReadingType = unitConversionNode.getTargetReadingType();
        this.unitConversionActive = true;
        SqlFragment expressionBuilder = unitConversionNode.getExpressionNode().accept(this);
        this.unitConversionActive = false;
        return sourceReadingType.buildSqlUnitConversion(this.mode, expressionBuilder, targetReadingType);
    }

    @Override
    public SqlFragment visitOperation(OperationNode operationNode) {
        SqlBuilder fragment = new SqlBuilder("(");
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
        return fragment;
    }

    @Override
    public SqlFragment visitFunctionCall(FunctionCallNode functionCall) {
        List<SqlFragment> arguments = functionCall.getArguments().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        SqlBuilder fragment = new SqlBuilder();
        Function function = functionCall.getFunction();
        function.appendTo(fragment, arguments);
        return fragment;
    }

    @Override
    public SqlFragment visitVirtualRequirement(VirtualRequirementNode requirement) {
        SqlBuilder fragment = new SqlBuilder();
        if (this.unitConversionActive) {
            requirement.appendTo(fragment);
        } else {
            /* May sound silly but we are actually requesting the node
             * to append itself to the SqlFragment and apply unit conversion
             * because unit conversion is currently not active. */
            requirement.appendToWithUnitConversion(fragment);
        }
        return fragment;
    }

    @Override
    public SqlFragment visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        SqlBuilder fragment = new SqlBuilder();
        if (this.unitConversionActive) {
            deliverable.appendTo(fragment);
        } else {
            deliverable.appendToWithUnitConversion(fragment);
        }
        return fragment;
    }

    @Override
    public SqlFragment visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        SqlBuilder fragment = new SqlBuilder();
        aggregationNode
                .getFunction()
                .appendTo(
                        fragment,
                        Collections.singletonList(aggregationNode.getAggregatedExpression().accept(this)));
        return fragment;
    }

}