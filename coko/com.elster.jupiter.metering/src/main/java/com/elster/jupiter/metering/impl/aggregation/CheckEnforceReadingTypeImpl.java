/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.List;

/**
 * Provides an implementation for the {@link CheckEnforceReadingType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-03 (11:00)
 */
class CheckEnforceReadingTypeImpl implements CheckEnforceReadingType {
    private final VirtualReadingType readingType;

    CheckEnforceReadingTypeImpl(VirtualReadingType readingType) {
        super();
        this.readingType = readingType;
    }

    @Override
    public VirtualReadingType getReadingType() {
        return readingType;
    }

    @Override
    public Boolean forAll(List<ServerExpressionNode> expressions) {
        if (this.readingType.isUnsupported()) {
            return Boolean.FALSE;
        } else {
            return expressions.stream().allMatch(expression -> expression.accept(this));
        }
    }

    @Override
    public Boolean visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        throw new IllegalArgumentException("Inference of reading type is not supported for expert mode functions");
    }

    @Override
    public Boolean visitConstant(NumericalConstantNode constant) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitNull(NullNode nullNode) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitConstant(StringConstantNode constant) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitProperty(CustomPropertyNode property) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSqlFragment(SqlFragmentNode variable) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitVirtualRequirement(VirtualRequirementNode requirement) {
        return requirement.supports(this.getReadingType());
    }

    @Override
    public Boolean visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // This is just another reference to a deliverable that we can always aggregate to a different interval
        return true;
    }

    @Override
    public Boolean visitUnitConversion(UnitConversionNode unitConversionNode) {
        return UnitConversionSupport.areCompatibleForAutomaticUnitConversion(this.getReadingType(), unitConversionNode.getTargetReadingType());
    }

    @Override
    public Boolean visitOperation(OperationNode operationNode) {
        return operationNode.getLeftOperand().accept(this) && operationNode.getRightOperand().accept(this);
    }

    @Override
    public Boolean visitFunctionCall(FunctionCallNode functionCall) {
        return this.forAll(functionCall.getArguments());
    }

}