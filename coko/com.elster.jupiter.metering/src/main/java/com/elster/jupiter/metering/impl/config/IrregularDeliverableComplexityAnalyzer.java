/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import java.util.EnumSet;
import java.util.Set;

/**
 * Analyzes the complexity of an {@link ExpressionNode}
 * for an irregular {@link ReadingTypeDeliverableImpl}
 * by visiting each node.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-08 (17:18)
 */
class IrregularDeliverableComplexityAnalyzer implements ExpressionNode.Visitor<Void> {
    private boolean simple = true;
    private ReadingTypeRequirementNode requirement;

    boolean isSimple() {
        return this.simple;
    }

    @Override
    public Void visitConstant(ConstantNode constant) {
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public Void visitRequirement(ReadingTypeRequirementNode requirement) {
        if (this.requirement != null) {
            // Multiple requirements -> complex
            this.simple = false;
        } else {
            this.requirement = requirement;
            if (requirement.getReadingTypeRequirement().isRegular()) {
                // Requirement with regular reading type for an irregular deliverable -> complex
                this.simple = false;
            }
        }
        return null;
    }

    @Override
    public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        // Reference to another deliverable in formula -> complex
        this.simple = false;
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        operationNode.getLeftOperand().accept(this);
        operationNode.getRightOperand().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        if (this.allowedFunctions().contains(functionCall.getFunction())) {
            functionCall.getChildren().forEach(argument -> argument.accept(this));
        } else {
            this.simple = false;
        }
        return null;
    }

    private Set<Function> allowedFunctions() {
        return EnumSet.of(Function.MIN, Function.MAX, Function.POWER, Function.SQRT, Function.FIRST_NOT_NULL);
    }

}