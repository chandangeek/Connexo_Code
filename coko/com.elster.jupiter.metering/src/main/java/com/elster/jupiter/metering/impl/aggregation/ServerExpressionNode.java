package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.ExpressionNode;

/**
 * Adds behavior to {@link ExpressionNode} that is specific to
 * server side aggregation components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (09:44)
 */
public interface ServerExpressionNode extends ExpressionNode {

    interface ServerVisitor<T> extends ExpressionNode.Visitor<T> {
        T visitVirtualRequirement(VirtualRequirementNode requirement);
        T visitVirtualDeliverable(VirtualDeliverableNode deliverable);
    }

    <T> T accept(ServerVisitor<T> visitor);

}