package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.AbstractNode;
import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.FunctionCallNode;
import com.elster.jupiter.metering.impl.config.OperationNode;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.ServerVisitor}
 * and returns the name of the SQL table (as String) that holds the data
 * of the visited {@link com.elster.jupiter.metering.impl.config.ExpressionNode}
 * or <code>null</code> if the ExpressionNode is not backed by a SQL table.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
public class FromClauseForExpressionNode extends VirtualVisitor<String> {

    @Override
    public String visitConstant(ConstantNode constant) {
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
    public String visitOperation(OperationNode operationNode) {
        return this.findFirst(Arrays.asList(
                        operationNode.getLeftOperand(),
                        operationNode.getRightOperand()));
    }

    @Override
    public String visitFunctionCall(FunctionCallNode functionCall) {
        return this.findFirst(functionCall.getChildren());
    }

    private String findFirst(List<AbstractNode> children) {
        return children
                .stream()
                .map(child -> child.accept(this))
                .findFirst().orElse(null);

    }

}