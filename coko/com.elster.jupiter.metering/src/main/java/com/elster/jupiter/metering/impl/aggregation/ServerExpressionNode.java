/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

/**
 * Like {@link ExpressionNode} but models behavior
 * that is specific to server side aggregation components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (09:44)
 */
interface ServerExpressionNode {

    interface Visitor<T> {
        T visitNull(NullNode nullNode);

        T visitConstant(NumericalConstantNode constant);

        T visitConstant(StringConstantNode constant);

        T visitProperty(CustomPropertyNode property);

        T visitSqlFragment(SqlFragmentNode variable);

        T visitVirtualRequirement(VirtualRequirementNode requirement);

        T visitVirtualDeliverable(VirtualDeliverableNode deliverable);

        T visitUnitConversion(UnitConversionNode unitConversionNode);

        T visitOperation(OperationNode operationNode);

        T visitFunctionCall(FunctionCallNode functionCall);

        T visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode);
    }

    <T> T accept(Visitor<T> visitor);

    IntermediateDimension getIntermediateDimension();

}