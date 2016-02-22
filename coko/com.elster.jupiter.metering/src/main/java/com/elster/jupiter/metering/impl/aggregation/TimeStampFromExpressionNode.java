package com.elster.jupiter.metering.impl.aggregation;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * and returns a SQL construct (as String) that provides the UTC timestamp for the visited
 * {@link com.elster.jupiter.metering.impl.config.ExpressionNode}
 * or <code>0</code> if the ExpressionNode cannot provide a UTC timestamp.
 * A {@link NumericalConstantNode} is a good example of that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
public class TimeStampFromExpressionNode implements ServerExpressionNode.Visitor<String> {

    /**
     * The value that will be used for expression that cannot support UTC timestamps.
     * Good examples of such expressions are:
     * <ul>
     * <li>NumericalConstantNode</li>
     * <li>StringConstantNode</li>
     * </ul>
     */
    private static final String FIXED_TIMESTAMP_VALUE = "0";

    @Override
    public String visitConstant(NumericalConstantNode constant) {
        return FIXED_TIMESTAMP_VALUE;
    }

    @Override
    public String visitConstant(StringConstantNode constant) {
        return FIXED_TIMESTAMP_VALUE;
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        return deliverable.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName();
    }

    @Override
    public String visitVirtualRequirement(VirtualRequirementNode requirement) {
        return requirement.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName();
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
                .findFirst().orElse(null);

    }

}