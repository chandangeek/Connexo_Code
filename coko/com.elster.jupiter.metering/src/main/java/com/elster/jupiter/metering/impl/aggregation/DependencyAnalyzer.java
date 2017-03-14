/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Analyzes the dependencies between the {@link ReadingTypeDeliverable}s
 * of a {@link MetrologyContract} and returns the deliverables in the correct
 * order such that when Y depends on X, X is returned before Y.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-28 (14:57)
 */
class DependencyAnalyzer implements ExpressionNode.Visitor<Void> {
    private final MetrologyContract contract;
    private Set<ReadingTypeDeliverable> deliverables;

    static DependencyAnalyzer forAnalysisOf(MetrologyContract contract) {
        return new DependencyAnalyzer(contract);
    }

    private DependencyAnalyzer(MetrologyContract contract) {
        super();
        this.contract = contract;
    }

    /**
     * Returns the {@link ReadingTypeDeliverable}s from the {@link MetrologyContract}
     * in the correct order such that when Y depends on X, X is returned before Y.
     *
     * @return The List of ReadingTypeDeliverable
     */
    List<ReadingTypeDeliverable> getDeliverables() {
        if (this.deliverables == null) {
            /* Use LinkedHashSet as that respects the order in which they were added
             * but avoids adding the same deliverable a second time. */
            this.deliverables = new LinkedHashSet<>();
            this.contract.getDeliverables().forEach(this::visit);
        }
        return ImmutableList.copyOf(this.deliverables);
    }

    private void visit(ReadingTypeDeliverable deliverable) {
        deliverable.getFormula().getExpressionNode().accept(this);
        this.deliverables.add(deliverable);
    }

    @Override
    public Void visitConstant(ConstantNode constant) {
        return null;
    }

    @Override
    public Void visitRequirement(ReadingTypeRequirementNode requirementNode) {
        return null;
    }

    @Override
    public Void visitDeliverable(ReadingTypeDeliverableNode deliverableNode) {
        /* Depth first visit of tree guarantees that deliverables
         * that are not using other deliverables (i.e. the leafs)
         * are added to the list of deliverables first. */
        this.visit(deliverableNode.getReadingTypeDeliverable());
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
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
        functionCall.getChildren().forEach(child -> child.accept(this));
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }
}