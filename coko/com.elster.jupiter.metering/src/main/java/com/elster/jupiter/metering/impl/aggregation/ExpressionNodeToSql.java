package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;
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
public class ExpressionNodeToSql implements ServerExpressionNode.Visitor<SqlFragment> {

    @Override
    public SqlFragment visitConstant(NumericalConstantNode constant) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.addObject(constant.getValue());
        return fragment;
    }

    @Override
    public SqlFragment visitNull(NullNode nullNode) {
        SqlBuilder fragment = new SqlBuilder();
        //todo
        return fragment;
    }

    @Override
    public SqlFragment visitConstant(StringConstantNode constant) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.addObject(constant.getValue());
        return fragment;
    }

    @Override
    public SqlFragment visitVariable(VariableReferenceNode variable) {
        return new TextFragment(variable.getName());
    }

    @Override
    public SqlFragment visitOperation(OperationNode operationNode) {
        SqlBuilder fragment = new SqlBuilder("(");
        operationNode
                .getOperator()
                .appendTo(
                        fragment,
                        operationNode.getLeftOperand().accept(this),
                        operationNode.getRightOperand().accept(this));
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
        requirement.appendTo(fragment);
        return fragment;
    }

    @Override
    public SqlFragment visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        SqlBuilder fragment = new SqlBuilder();
        deliverable.appendTo(fragment);
        return fragment;
    }

    @Override
    public SqlFragment visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        SqlBuilder fragment = new SqlBuilder();
        aggregationNode.getFunction().appendTo(fragment, Collections.singletonList(aggregationNode.getAggregatedExpression().accept(this)));
        return fragment;
    }

}