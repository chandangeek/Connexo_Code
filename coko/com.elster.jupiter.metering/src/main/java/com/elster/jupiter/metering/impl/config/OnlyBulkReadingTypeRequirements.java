/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import static com.elster.jupiter.cbo.Accumulation.BULKQUANTITY;

/**
 * Checks that an {@link ExpressionNode}
 * only contains bulk reading type requirements or deliverables.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-25 (11:44)
 */
class OnlyBulkReadingTypeRequirements implements ExpressionNode.Visitor<Boolean> {

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
        if (requirement.getReadingTypeRequirement() instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement readingTypeRequirement = (FullySpecifiedReadingTypeRequirement) requirement.getReadingTypeRequirement();
            return this.isBulk(readingTypeRequirement.getReadingType());
        } else {
            return Boolean.TRUE;
        }
    }

    @Override
    public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return this.isBulk(deliverable.getReadingTypeDeliverable().getReadingType());
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

    private boolean isBulk(ReadingType readingType) {
        return BULKQUANTITY.equals(readingType.getAccumulation());
    }

}