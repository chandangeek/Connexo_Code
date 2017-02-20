/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Visits an tree of expressions and flattens the tree
 * using pre-order traversal.
 * For clarity's sake, this means that the node is visited
 * first and then the child nodes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-21 (11:35)
 */
class Flatten implements ServerExpressionNode.Visitor<Void> {

    private final List<ServerExpressionNode> flattened = new ArrayList<>();

    List<ServerExpressionNode> getFlattened() {
        return Collections.unmodifiableList(flattened);
    }

    @Override
    public Void visitConstant(NumericalConstantNode constant) {
        this.flattened.add(constant);
        return null;
    }

    @Override
    public Void visitConstant(StringConstantNode constant) {
        this.flattened.add(constant);
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        this.flattened.add(property);
        return null;
    }

    @Override
    public Void visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        this.flattened.add(slp);
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        this.flattened.add(nullNode);
        return null;
    }

    @Override
    public Void visitSqlFragment(SqlFragmentNode variable) {
        this.flattened.add(variable);
        return null;
    }

    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        this.flattened.add(requirement);
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        this.flattened.add(deliverable);
        return null;
    }

    @Override
    public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
        this.flattened.add(unitConversionNode);
        unitConversionNode.getExpressionNode().accept(this);
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        this.flattened.add(operationNode);
        operationNode.getLeftOperand().accept(this);
        operationNode.getRightOperand().accept(this);
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        this.flattened.add(functionCall);
        functionCall.getArguments().forEach(each -> each.accept(this));
        return null;
    }

    @Override
    public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        this.flattened.add(aggregationNode);
        aggregationNode.getAggregatedExpression().accept(this);
        return null;
    }

}