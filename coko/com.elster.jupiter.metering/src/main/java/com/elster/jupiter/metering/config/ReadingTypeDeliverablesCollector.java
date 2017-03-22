/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * to find out all 'underlying' {@link ReadingTypeDeliverable} instances,
 * i.e. contained directly in the visited {@link ExpressionNode}
 * or, recursively, in the visited node and all formulas of found directly underlying deliverables.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
public class ReadingTypeDeliverablesCollector implements ExpressionNode.Visitor<Set<ReadingTypeDeliverable>> {
    private final Map<ReadingTypeDeliverable, Set<ReadingTypeDeliverable>> dependencyMap;

    private ReadingTypeDeliverablesCollector(Map<ReadingTypeDeliverable, Set<ReadingTypeDeliverable>> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    /**
     * Creates an instance of collector to find out all {@link ReadingTypeDeliverable} instances
     * directly contained in the visited {@link ExpressionNode}.
     * @return An instance of {@link ReadingTypeDeliverablesCollector}.
     */
    public static ReadingTypeDeliverablesCollector flat() {
        return new ReadingTypeDeliverablesCollector(null);
    }

    /**
     * Creates an instance of collector to recursively find out all {@link ReadingTypeDeliverable} instances
     * contained in the visited {@link ExpressionNode} and formulas of underlying deliverables.
     * @param dependencyMap A map that contains already known reading type deliverables with sets of their underlying deliverables, can be empty.
     * This map is used as master source during calculation of underlying deliverables for the given expression node
     * and is meanwhile supplied with new entries, thus <strong>must be mutable</strong>.
     * @return An instance of {@link ReadingTypeDeliverablesCollector}.
     */
    public static ReadingTypeDeliverablesCollector recursive(Map<ReadingTypeDeliverable, Set<ReadingTypeDeliverable>> dependencyMap) {
        return new ReadingTypeDeliverablesCollector(dependencyMap);
    }

    @Override
    public Set<ReadingTypeDeliverable> visitConstant(ConstantNode constant) {
        return Collections.emptySet();
    }

    @Override
    public Set<ReadingTypeDeliverable> visitProperty(CustomPropertyNode property) {
        return Collections.emptySet();
    }

    @Override
    public Set<ReadingTypeDeliverable> visitRequirement(ReadingTypeRequirementNode requirement) {
        return Collections.emptySet();
    }

    @Override
    public Set<ReadingTypeDeliverable> visitDeliverable(ReadingTypeDeliverableNode deliverableNode) {
        ReadingTypeDeliverable deliverable = deliverableNode.getReadingTypeDeliverable();
        if (dependencyMap == null) {
            return Collections.singleton(deliverable);
        } else {
            Set<ReadingTypeDeliverable> underlyingDeliverables = dependencyMap.computeIfAbsent(deliverable,
                    del -> del.getFormula().getExpressionNode().accept(this));
            return Stream.concat(Stream.of(deliverable), underlyingDeliverables.stream())
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public Set<ReadingTypeDeliverable> visitOperation(OperationNode operationNode) {
        return getDeliverablesFromChildrenNodes(operationNode);
    }

    @Override
    public Set<ReadingTypeDeliverable> visitFunctionCall(FunctionCallNode functionCall) {
        return getDeliverablesFromChildrenNodes(functionCall);
    }

    @Override
    public Set<ReadingTypeDeliverable> visitNull(NullNode nullNode) {
        return Collections.emptySet();
    }

    private Set<ReadingTypeDeliverable> getDeliverablesFromChildrenNodes(ExpressionNode node) {
        return node.getChildren()
                .stream()
                .map(child -> child.accept(this))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
