/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import java.util.ArrayList;
import java.util.List;

public class ReadingTypeRequirementsCollector implements ExpressionNode.Visitor<Void> {
    private List<ReadingTypeRequirement> readingTypeRequirements = new ArrayList<>();

    @Override
    public Void visitConstant(ConstantNode constant) {
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        return null;
    }

    @Override
    public Void visitRequirement(ReadingTypeRequirementNode requirement) {
        this.readingTypeRequirements.add(requirement.getReadingTypeRequirement());
        return null;
    }

    @Override
    public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        operationNode.getChildren().forEach(n -> n.accept(this));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getChildren().forEach(n -> n.accept(this));
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    public List<ReadingTypeRequirement> getReadingTypeRequirements() {
        return this.readingTypeRequirements;
    }
}