package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.AbstractNode;
import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.FunctionCallNode;
import com.elster.jupiter.metering.impl.config.OperationNode;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link com.elster.jupiter.metering.impl.aggregation.ServerExpressionNode.ServerVisitor}
 * and returns a SQL construct (as String) that provides a LOCALDATE for the visited
 * {@link com.elster.jupiter.metering.impl.config.ExpressionNode}
 * or <code>null</code> if the ExpressionNode cannot provide such a LOCALDATE.
 * A {@link ConstantNode} is a good example of that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
public class LocalDateFromExpressionNode extends VirtualVisitor<String> {
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