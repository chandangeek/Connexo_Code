/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import java.util.Map;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * to find out dependency level of the visited {@link ExpressionNode},
 * that indicates how deeply it depends on other {@link ReadingTypeDeliverable ReadingTypeDeliverables}.
 * I.e. if no 'underlying' deliverable is found in the given expression, dependency level is 0,
 * if there's any number of {@link ReadingTypeDeliverableNode ReadingTypeDeliverableNodes} in the expression,
 * but none of their formulas contain deliverables, dependency level is 1,
 * otherwise dependency level is maximum of dependency levels of all underlying deliverables plus one.
 */
class DeliverableDependencyLevelRetriever implements ExpressionNode.Visitor<Integer> {
    private final Map<ReadingType, Integer> readingTypesWithDependencyLevels;

    /**
     * @param readingTypesWithDependencyLevels A map that contains already known reading type deliverables with their dependency levels, can be empty.
     * This map is used as master source during calculation of dependency level for the given expression node
     * and is meanwhile supplied with new entries, thus <strong>must be mutable</strong>.
     */
    DeliverableDependencyLevelRetriever(Map<ReadingType, Integer> readingTypesWithDependencyLevels) {
        this.readingTypesWithDependencyLevels = readingTypesWithDependencyLevels;
    }

    @Override
    public Integer visitConstant(ConstantNode constant) {
        return 0;
    }

    @Override
    public Integer visitRequirement(ReadingTypeRequirementNode requirement) {
        return 0;
    }

    @Override
    public Integer visitDeliverable(ReadingTypeDeliverableNode deliverableNode) {
        // current deliverable is dependent on another one, we take its dependency level and increase it by one
        ReadingTypeDeliverable deliverable = deliverableNode.getReadingTypeDeliverable();
        Integer dependencyLevel = readingTypesWithDependencyLevels.computeIfAbsent(deliverable.getReadingType(),
                readingType -> deliverable.getFormula().getExpressionNode().accept(this));
        return dependencyLevel + 1;
    }

    @Override
    public Integer visitProperty(CustomPropertyNode property) {
        return 0;
    }

    @Override
    public Integer visitOperation(OperationNode operationNode) {
        return visitChildren(operationNode);
    }

    @Override
    public Integer visitFunctionCall(FunctionCallNode functionCall) {
        return visitChildren(functionCall);
    }

    @Override
    public Integer visitNull(NullNode nullNode) {
        return 0;
    }

    private int visitChildren(ExpressionNode node) {
        // current deliverable can depend on several others, we take longest dependency chain,
        // i.e. maximum of dependency levels calculated based on all of them
        return node.getChildren()
                .stream()
                .mapToInt(child -> child.accept(this))
                .max()
                .orElse(0);
    }
}
