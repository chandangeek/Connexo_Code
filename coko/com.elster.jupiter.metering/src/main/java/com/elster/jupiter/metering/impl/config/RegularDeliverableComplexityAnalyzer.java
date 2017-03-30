/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

/**
 * Analyzes the complexity of an {@link ExpressionNode}
 * for a regular {@link ReadingTypeDeliverableImpl}
 * by visiting each node.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (09:49)
 */
class RegularDeliverableComplexityAnalyzer implements ExpressionNode.Visitor<Boolean> {

    @Override
    public Boolean visitConstant(ConstantNode constant) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitProperty(CustomPropertyNode property) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitNull(NullNode nullNode) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
        return requirement.getReadingTypeRequirement().isRegular();
    }

    @Override
    public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return deliverable.getReadingTypeDeliverable().getReadingType().isRegular();
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
                .allMatch(argument -> argument.accept(this));
    }

}