package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.Function;
import com.elster.jupiter.metering.impl.config.FunctionCallNode;
import com.elster.jupiter.metering.impl.config.OperationNode;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link com.elster.jupiter.metering.impl.aggregation.ServerExpressionNode.ServerVisitor}
 * interface that generates SQL for the visited {@link com.elster.jupiter.metering.impl.config.ExpressionNode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (12:41)
 */
public class ExpressionNodeToSql extends VirtualVisitor<SqlFragment> {

    @Override
    public SqlFragment visitConstant(ConstantNode constant) {
        SqlBuilder fragment = new SqlBuilder();
        fragment.addObject(constant.getValue());
        return fragment;
    }

    @Override
    public SqlFragment visitOperation(OperationNode operationNode) {
        SqlBuilder fragment = new SqlBuilder("(");
        fragment.add(operationNode.getLeftOperand().accept(this));
        operationNode.getOperator().appendTo(fragment);
        fragment.add(operationNode.getRightOperand().accept(this));
        fragment.append(")");
        return fragment;
    }

    @Override
    public SqlFragment visitFunctionCall(FunctionCallNode functionCall) {
        List<SqlFragment> arguments = functionCall.getChildren().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        SqlBuilder fragment = new SqlBuilder();
        Function function = functionCall.getFunction();
        if (function == null) {
            this.appendCustomFunction(functionCall, arguments, fragment);
        }
        else {
            function.appendTo(fragment, arguments);
        }
        return fragment;
    }

    private void appendCustomFunction(FunctionCallNode functionCall, List<SqlFragment> arguments, SqlBuilder fragment) {
        fragment.append(functionCall.getName());
        fragment.append("(");
        Iterator<SqlFragment> iterator = arguments.iterator();
        while (iterator.hasNext()) {
            SqlFragment argument = iterator.next();
            fragment.add(argument);
            if (iterator.hasNext()) {
                fragment.append(", ");
            }
        }
        fragment.append(")");
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

}