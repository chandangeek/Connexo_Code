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

    private static final String STARTTIMESTART_TIME = "starttime";
    private static final String END_TIME = "endtime";
    private final DataSourceTable defaultSourceTable;
    private String propertyTableName;
    private String timeSeriesTableName;

    FromClauseForExpressionNode(DataSourceTable defaultSourceTable) {
        this.defaultSourceTable = defaultSourceTable;
    }

    DataSourceTable getSource() {
        if (this.timeSeriesTableName != null) {
            return new TimeSeriesTable(this.timeSeriesTableName);
        } else if (this.propertyTableName != null) {
            return new CustomPropertyTable(this.propertyTableName);
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

    private static String timestampFrom(String tableName) {
        return fullyQualified(tableName, SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    private static String fullyQualified(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    private static class TimeSeriesTable implements DataSourceTable {
        private final String name;

        private TimeSeriesTable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String propertiesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (    " + fullyQualified(tableName, STARTTIMESTART_TIME) + " < " + timestampFrom(this.name) +
                    "                            AND " + timestampFrom(this.name) + " <= " + fullyQualified(tableName, END_TIME) + ")";
        }

        @Override
        public String timeSeriesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON " + timestampFrom(tableName) + " = " + timestampFrom(this.name);
        }
    }

    private static class CustomPropertyTable implements DataSourceTable {
        private final String name;

        private CustomPropertyTable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String timeSeriesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (    " + fullyQualified(this.name, STARTTIMESTART_TIME) + " < " + timestampFrom(tableName) +
                   "                            AND " + timestampFrom(tableName) + " <= " + fullyQualified(this.name, END_TIME) + ")";
        }

        @Override
        public String propertiesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (   (    " + fullyQualified(this.name, STARTTIMESTART_TIME) + " <= " + fullyQualified(tableName, STARTTIMESTART_TIME) +
                   "                                AND " + fullyQualified(this.name, END_TIME) + " >= " + fullyQualified(tableName, STARTTIMESTART_TIME) + ")" +
                   "                            OR (    " + fullyQualified(tableName, STARTTIMESTART_TIME) + " <= " + fullyQualified(this.name, STARTTIMESTART_TIME) +
                   "                                AND " + fullyQualified(tableName, END_TIME) + " >= " + fullyQualified(this.name, STARTTIMESTART_TIME) + "))";
        }
    }

}