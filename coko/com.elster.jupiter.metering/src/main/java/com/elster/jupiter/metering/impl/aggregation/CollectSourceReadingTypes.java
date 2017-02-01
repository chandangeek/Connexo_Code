/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that collects the source reading type of each of the nodes in the tree.
 * <p>
 * Note that it is important to run this after {@link InferReadingType} as that will
 * set the target reading that can then be matched against the available channels
 * in the {@link com.elster.jupiter.metering.MeterActivation} to find the best one.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-03 (15:12)
 */
class CollectSourceReadingTypes implements ServerExpressionNode.Visitor<VirtualReadingType> {
    private final Set<VirtualReadingType> readingTypes = new HashSet<>();

    public Set<VirtualReadingType> getReadingTypes() {
        return Collections.unmodifiableSet(this.readingTypes);
    }

    @Override
    public VirtualReadingType visitNull(NullNode nullNode) {
        // This node does not have a preferred reading type so don't add one to the set
        return null;
    }

    @Override
    public VirtualReadingType visitConstant(NumericalConstantNode constant) {
        // This node does not have a preferred reading type so don't add one to the set
        return null;
    }

    @Override
    public VirtualReadingType visitConstant(StringConstantNode constant) {
        // This node does not have a preferred reading type so don't add one to the set
        return null;
    }

    @Override
    public VirtualReadingType visitProperty(CustomPropertyNode property) {
        // This node does not have a preferred reading type so don't add one to the set
        return null;
    }

    @Override
    public VirtualReadingType visitSqlFragment(SqlFragmentNode variable) {
        // This node does not have a preferred reading type so don't add one to the set
        return null;
    }

    @Override
    public VirtualReadingType visitVirtualRequirement(VirtualRequirementNode requirement) {
        this.readingTypes.add(requirement.getSourceReadingType());
        return null;
    }

    @Override
    public VirtualReadingType visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        this.readingTypes.add(deliverable.getSourceReadingType());
        return null;
    }

    @Override
    public VirtualReadingType visitUnitConversion(UnitConversionNode unitConversionNode) {
        throw new IllegalStateException("Not expecting any UnitConversionNodes as this component is part of the process that creates them");
    }

    @Override
    public VirtualReadingType visitOperation(OperationNode operationNode) {
        operationNode.getLeftOperand().accept(this);
        operationNode.getRightOperand().accept(this);
        if (Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            operationNode.getSafeDivisor().accept(this);
        }
        return null;
    }

    @Override
    public VirtualReadingType visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getArguments().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public VirtualReadingType visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        aggregationNode.getAggregatedExpression().accept(this);
        return null;
    }
}
