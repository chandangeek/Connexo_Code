package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;
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
public class ExpressionNodeToString implements ServerExpressionNode.Visitor<String> {

    @Override
    public String visitConstant(NumericalConstantNode constant) {
        return String.valueOf(constant.getValue());
    }

    @Override
    public String visitNull(NullNodeImpl nullNode) {
        return "null";
    }

    @Override
    public String visitConstant(StringConstantNode constant) {
        return constant.getValue();
    }

    @Override
    public String visitVariable(VariableReferenceNode variable) {
        return variable.getName();
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
        requirement.appendTo(fragment);
        return fragment.getText();
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        SqlBuilder fragment = new SqlBuilder();
        deliverable.appendTo(fragment);
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