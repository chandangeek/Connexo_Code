/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

/**
 * Checks that an {@link ExpressionNode}
 * for a regular {@link ReadingTypeDeliverableImpl}
 * does not contain any bulk reading type requirements or deliverables.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-18 (10:24)
 */
class NoBulkReadingTypeRequirements implements ExpressionNode.Visitor<Boolean> {

    @Override
    public Boolean visitConstant(ConstantNode constant) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitProperty(CustomPropertyNode property) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitNull(NullNode nullNode) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
        if (requirement.getReadingTypeRequirement() instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement readingTypeRequirement = (FullySpecifiedReadingTypeRequirement) requirement.getReadingTypeRequirement();
            return readingTypeRequirement.getReadingType().getAccumulation().equals(Accumulation.BULKQUANTITY);
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return deliverable.getReadingTypeDeliverable().getReadingType().getAccumulation().equals(Accumulation.BULKQUANTITY);
    }

    @Override
    public Boolean visitOperation(OperationNode operationNode) {
        return operationNode.getLeftOperand().accept(this)
            || operationNode.getRightOperand().accept(this);
    }

    @Override
    public Boolean visitFunctionCall(FunctionCallNode functionCall) {
        return functionCall
                .getChildren()
                .stream()
                .anyMatch(argument -> argument.accept(this));
    }

}