package com.elster.jupiter.metering.impl.aggregation;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that return all requirements for an expression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-04 (15:40)
 */
class RequirementsFromExpressionNode implements ServerExpressionNode.Visitor<List<VirtualRequirementNode>> {
    @Override
    public List<VirtualRequirementNode> visitVirtualRequirement(VirtualRequirementNode requirement) {
        List<VirtualRequirementNode> result = new ArrayList();
        result.add(requirement);
        return result;
    }

    @Override
    public List<VirtualRequirementNode> visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        List<VirtualRequirementNode> result = new ArrayList();
        result.addAll(deliverable.accept(this));
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitConstant(NumericalConstantNode constant) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitConstant(StringConstantNode constant) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitProperty(CustomPropertyNode property) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitSqlFragment(SqlFragmentNode variable) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitNull(NullNode nullNode) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitUnitConversion(UnitConversionNode unitConversionNode) {
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitOperation(OperationNode operation) {
        List<VirtualRequirementNode> result = new ArrayList();
        result.addAll(operation.getLeftOperand().accept(this));
        result.addAll(operation.getRightOperand().accept(this));
        return new ArrayList();
    }

    @Override
    public List<VirtualRequirementNode> visitFunctionCall(FunctionCallNode functionCall) {
        List<VirtualRequirementNode> result = new ArrayList();
        functionCall.getArguments().forEach(child -> result.addAll(child.accept(this)));
        return result;
    }

    @Override
    public List<VirtualRequirementNode> visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return new ArrayList();
    }

}
