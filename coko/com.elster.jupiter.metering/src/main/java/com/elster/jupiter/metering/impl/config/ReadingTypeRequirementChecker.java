package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import java.util.ArrayList;
import java.util.List;

public class ReadingTypeRequirementChecker implements ExpressionNode.Visitor<ReadingTypeRequirementChecker> {
    private List<ReadingTypeRequirement> readingTypeRequirements = new ArrayList<>();

    @Override
    public ReadingTypeRequirementChecker visitConstant(ConstantNode constant) {
        constant.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    @Override
    public ReadingTypeRequirementChecker visitRequirement(ReadingTypeRequirementNode requirement) {
        this.readingTypeRequirements.add(requirement.getReadingTypeRequirement());
        requirement.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    @Override
    public ReadingTypeRequirementChecker visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        deliverable.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    @Override
    public ReadingTypeRequirementChecker visitOperation(OperationNode operationNode) {
        operationNode.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    @Override
    public ReadingTypeRequirementChecker visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    @Override
    public ReadingTypeRequirementChecker visitNull(NullNode nullNode) {
        nullNode.getChildren().forEach(n -> n.accept(this));
        return this;
    }

    public List<ReadingTypeRequirement> getReadingTypeRequirements() {
        return this.readingTypeRequirements;
    }
}
