/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that tests if the {@link ExpressionNode}s contains only custom properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-01 (14:15)
 */
class ContainsOnlyCustomProperties implements ExpressionNode.Visitor<Boolean> {

    @Override
    public Boolean visitConstant(ConstantNode constant) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitProperty(CustomPropertyNode property) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitOperation(OperationNode operationNode) {
        return operationNode.getLeftOperand().accept(this)
            && operationNode.getRightOperand().accept(this);
    }

    @Override
    public Boolean visitFunctionCall(FunctionCallNode functionCall) {
        return functionCall
                    .getChildren()
                    .stream()
                    .allMatch(child -> child.accept(this));
    }

    @Override
    public Boolean visitNull(NullNode nullNode) {
        return Boolean.TRUE;
    }

}