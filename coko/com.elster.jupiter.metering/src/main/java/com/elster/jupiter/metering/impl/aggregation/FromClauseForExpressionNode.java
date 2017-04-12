/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor}
 * and returns the name of the SQL table (as String) that holds the data
 * of the visited {@link ExpressionNode}
 * or <code>null</code> if the ExpressionNode is not backed by a SQL table.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
class FromClauseForExpressionNode implements ServerExpressionNode.Visitor<Void> {

    private final DataSourceTable defaultSourceTable;
    private String propertyTableName;
    private String timeSeriesTableName;

    FromClauseForExpressionNode(DataSourceTable defaultSourceTable) {
        this.defaultSourceTable = defaultSourceTable;
    }

    DataSourceTable getSource() {
        if (this.timeSeriesTableName != null) {
            return DataSourceTableFactory.timeSeries(this.timeSeriesTableName);
        } else if (this.propertyTableName != null) {
            return DataSourceTableFactory.customProperties(this.propertyTableName);
        } else {
            return this.defaultSourceTable;
        }
    }

    @Override
    public Void visitConstant(NumericalConstantNode constant) {
        return null;
    }

    @Override
    public Void visitConstant(StringConstantNode constant) {
        return null;
    }

    @Override
    public Void visitProperty(CustomPropertyNode property) {
        this.propertyTableName = property.sqlName();
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public Void visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // First one wins (for backwards compatibility)
        if (this.timeSeriesTableName == null) {
            this.timeSeriesTableName = deliverable.sqlName();
        }
        return null;
    }

    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        // First one wins (for backwards compatibility)
        if (this.timeSeriesTableName == null) {
            this.timeSeriesTableName = requirement.sqlName();
        }
        return null;
    }

    @Override
    public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getExpressionNode().accept(this);
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        return this.acceptAll(Arrays.asList(
                operationNode.getLeftOperand(),
                operationNode.getRightOperand()));
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        return this.acceptAll(functionCall.getArguments());
    }

    private Void acceptAll(List<ServerExpressionNode> children) {
        children.forEach(child -> child.accept(this));
        return null;
    }

    @Override
    public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

}