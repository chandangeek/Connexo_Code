package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;

/**
 * Serves as an abstract class for components that intend to implement
 * the {@link com.elster.jupiter.metering.impl.aggregation.ServerExpressionNode.ServerVisitor}
 * interface but are not expecting the {@link com.elster.jupiter.metering.impl.config.ExpressionNode}s
 * to contain {@link ReadingTypeRequirementNode}s or {@link ReadingTypeDeliverableNode}s.
 * Instead, they are expecting to work in the "virtualized" version of these nodes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:34)
 */
public abstract class VirtualVisitor<T> implements ServerExpressionNode.ServerVisitor<T> {

    @Override
    public T visitRequirement(ReadingTypeRequirementNode requirement) {
        this.unexpectedRequirementNode();
        return null;    // Above will have thrown an exception so merely satisfying the compiler here
    }

    private void unexpectedRequirementNode() throws IllegalArgumentException {
        throw new IllegalArgumentException("Not expecting formulas to contain actual requirement nodes, invoke this component when those have been replaced with VirtualReadingTypeRequirement");
    }

    @Override
    public T visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        this.unexpectedDeliverableNode();
        return null;    // Above will have thrown an exception so merely satisfying the compiler here
    }

    private IntervalLength unexpectedDeliverableNode() {
        throw new IllegalArgumentException("Not expecting formulas to contain actual deliverable nodes, invoke this component when those have been replaced with VirtualReadingTypeDeliverable");
    }

}