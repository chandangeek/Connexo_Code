/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Builds the description of an {@link ExpressionNode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-12 (15:18)
 */
class FormulaDescriptionBuilder implements ExpressionNode.Visitor<String> {
    private final Thesaurus thesaurus;

    FormulaDescriptionBuilder(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String visitConstant(ConstantNode constant) {
        return String.valueOf(constant.getValue());
    }

    @Override
    public String visitRequirement(ReadingTypeRequirementNode requirement) {
        return "input(" + requirement.getReadingTypeRequirement().getName() + ")";
    }

    @Override
    public String visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return "output(" + deliverable.getReadingTypeDeliverable().getName() + ")";
    }

    @Override
    public String visitProperty(CustomPropertyNode property) {
        return property.getPropertySpec().getDisplayName();
    }

    @Override
    public String visitOperation(OperationNode operationNode) {
        return OperatorTranslationKey.format(this.thesaurus, operationNode, this);
    }

    @Override
    public String visitFunctionCall(FunctionCallNode functionCall) {
        return FunctionTranslationKey.format(this.thesaurus, functionCall, this);
    }

    @Override
    public String visitNull(NullNode nullNode) {
        return "null";
    }

}