/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that return all requirements for an expression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-04 (15:40)
 */
class RequirementsFromExpressionNode implements ServerExpressionNode.Visitor<List<VirtualRequirementNode>> {
    private final boolean recursiveOnDeliverables;

    public static RequirementsFromExpressionNode nonRecursiveOnDeliverables() {
        return new RequirementsFromExpressionNode(false);
    }

    public static RequirementsFromExpressionNode recursiveOnDeliverables() {
        return new RequirementsFromExpressionNode(true);
    }

    private RequirementsFromExpressionNode(boolean recursiveOnDeliverables) {
        super();
        this.recursiveOnDeliverables = recursiveOnDeliverables;
    }

    @Override
    public List<VirtualRequirementNode> visitVirtualRequirement(VirtualRequirementNode requirement) {
        return Collections.singletonList(requirement);
    }

    @Override
    public List<VirtualRequirementNode> visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        if (this.recursiveOnDeliverables) {
            return deliverable.nestedRequirements(this);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<VirtualRequirementNode> visitConstant(NumericalConstantNode constant) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitConstant(StringConstantNode constant) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitProperty(CustomPropertyNode property) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitSqlFragment(SqlFragmentNode variable) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitNull(NullNode nullNode) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitUnitConversion(UnitConversionNode unitConversionNode) {
        return Collections.emptyList();
    }

    @Override
    public List<VirtualRequirementNode> visitOperation(OperationNode operation) {
        List<VirtualRequirementNode> result = new ArrayList<>();
        result.addAll(operation.getLeftOperand().accept(this));
        result.addAll(operation.getRightOperand().accept(this));
        return result;
    }

    @Override
    public List<VirtualRequirementNode> visitFunctionCall(FunctionCallNode functionCall) {
        List<VirtualRequirementNode> result = new ArrayList<>();
        functionCall.getArguments().forEach(child -> result.addAll(child.accept(this)));
        return result;
    }

    @Override
    public List<VirtualRequirementNode> visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return Collections.emptyList();
    }

}