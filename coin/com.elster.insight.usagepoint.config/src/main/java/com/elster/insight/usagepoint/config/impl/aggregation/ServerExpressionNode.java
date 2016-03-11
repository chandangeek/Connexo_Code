package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Adds behavior to {@link ExpressionNode} that is specific to
 * server side components. We anticipate that ExpressionNode
 * will be published and made available to the UI components
 * to visualize and edit the expression tree of a Formula.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (09:44)
 */
interface ServerExpressionNode extends ExpressionNode {

    interface ServerVisitor<T> extends ExpressionNode.Visitor<T> {
        T visitVirtualRequirement(VirtualRequirementNode requirement);
        T visitVirtualDeliverable(VirtualDeliverableNode deliverable);
    }

    <T> T accept(ServerVisitor<T> visitor);

}