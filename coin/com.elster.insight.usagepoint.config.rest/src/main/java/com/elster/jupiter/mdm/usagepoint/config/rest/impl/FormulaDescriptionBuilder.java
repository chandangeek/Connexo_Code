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
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
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
    private ReadingTypeDeliverable deliverable;
    private final TimeOfUseFormatter timeOfUseFormatter;
    private final Thesaurus thesaurus;

    FormulaDescriptionBuilder(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.timeOfUseFormatter = new NoTimeOfUse();
    }

    FormulaDescriptionBuilder(ReadingTypeDeliverable deliverable, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.deliverable = deliverable;
        if (deliverable.getReadingType().getTou() == 0) {
            this.timeOfUseFormatter = new NoTimeOfUse();
        } else {
            this.timeOfUseFormatter = new WithTimeOfUse();
        }
    }

    @Override
    public String visitConstant(ConstantNode constant) {
        return String.valueOf(constant.getValue());
    }

    @Override
    public String visitRequirement(ReadingTypeRequirementNode requirement) {
        return this.timeOfUseFormatter.format("input(" + requirement.getReadingTypeRequirement().getName() + ")");
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

    private interface TimeOfUseFormatter {
        String format(String description);
    }

    private static class NoTimeOfUse implements TimeOfUseFormatter {
        @Override
        public String format(String description) {
            return description;
        }
    }

    private class WithTimeOfUse implements TimeOfUseFormatter {
        @Override
        public String format(String description) {
            return thesaurus
                        .getFormat(VirtualFunctionTranslationKey.TOU)
                        .format(
                            deliverable.getReadingType().getTou(),
                            description);
        }
    }
}