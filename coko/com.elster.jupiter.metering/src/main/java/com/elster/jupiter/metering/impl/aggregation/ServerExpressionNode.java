package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.config.ExpressionNode;

/**
 * Like {@link ExpressionNode} but models behavior
 * that is specific to server side aggregation components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (09:44)
 */
public interface ServerExpressionNode {

    interface Visitor<T> {
        T visitConstant(NumericalConstantNode constant);
        T visitConstant(StringConstantNode constant);
        T visitVirtualRequirement(VirtualRequirementNode requirement);
        T visitVirtualDeliverable(VirtualDeliverableNode deliverable);
        T visitOperation(OperationNode operationNode);
        T visitFunctionCall(FunctionCallNode functionCall);
    }

    <T> T accept(Visitor<T> visitor);

}