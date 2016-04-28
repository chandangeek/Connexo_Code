package com.elster.jupiter.metering.impl.aggregation;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that extracts the {@link VirtualReadingType} from a {@link ServerExpressionNode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-28 (15:51)
 */
public class GetTargetReadingType implements ServerExpressionNode.Visitor<VirtualReadingType> {

    private final VirtualReadingType defaultReadingType;

    public GetTargetReadingType(VirtualReadingType defaultReadingType) {
        this.defaultReadingType = defaultReadingType;
    }

    @Override
    public VirtualReadingType visitNull(NullNode nullNode) {
        return this.defaultReadingType;
    }

    @Override
    public VirtualReadingType visitConstant(NumericalConstantNode constant) {
        return this.defaultReadingType;
    }

    @Override
    public VirtualReadingType visitConstant(StringConstantNode constant) {
        return this.defaultReadingType;
    }

    @Override
    public VirtualReadingType visitSqlFragment(SqlFragmentNode variable) {
        return this.defaultReadingType;
    }

    @Override
    public VirtualReadingType visitVirtualRequirement(VirtualRequirementNode requirement) {
        return requirement.getTargetReadingType();
    }

    @Override
    public VirtualReadingType visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        return deliverable.getTargetReadingType();
    }

    @Override
    public VirtualReadingType visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getTargetReadingType();
    }

    @Override
    public VirtualReadingType visitOperation(OperationNode operationNode) {
        List<ServerExpressionNode> children;
        if (Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            children = Arrays.asList(operationNode.getLeftOperand(), operationNode.getRightOperand(), operationNode.getSafeDivisor());
        } else {
            children = Arrays.asList(operationNode.getLeftOperand(), operationNode.getRightOperand());
        }
        return this.visitChildren(children);
    }

    @Override
    public VirtualReadingType visitFunctionCall(FunctionCallNode functionCall) {
        return this.visitChildren(functionCall.getArguments());
    }

    @Override
    public VirtualReadingType visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

    private VirtualReadingType visitChildren(List<ServerExpressionNode> children) {
        return children
                .stream()
                .map(child -> child.accept(this))
                .filter(virtualReadingType ->  virtualReadingType != this.defaultReadingType)
                .distinct()
                .findFirst()
                .orElse(this.defaultReadingType);
    }

}