package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor}
 * and returns the name of the SQL table (as String) that holds the data
 * of the visited {@link ExpressionNode}
 * or <code>null</code> if the ExpressionNode is not backed by a SQL table.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (13:28)
 */
class JoinClausesForExpressionNode implements ServerExpressionNode.Visitor<Void> {

    private String sourceTableName;
    private Collection<JoinClause> joinClauses = new ArrayList<>();
    private Set<String> joinTableNames = new HashSet<>();

    JoinClausesForExpressionNode(String sourceTableName) {
        super();
        this.sourceTableName = sourceTableName;
    }

    List<String> joinClauses() {
        return this.joinClauses
                .stream()
                .map(each -> each.joinWith(" JOIN ", this.sourceTableName))
                .collect(Collectors.toList());
    }

    private void visitTimeSeries(String tableName) {
        if (!this.sourceTableName.equals(tableName)) {
            this.add(new TimeSeriesJoinClause(tableName));
        }
    }

    private void visitCustomProperty(String tableName) {
        if (!this.sourceTableName.equals(tableName)) {
            this.add(new PropertiesJoinClause(tableName));
        }
    }

    private void add(JoinClause joinClause) {
        if (!this.joinTableNames.contains(joinClause.tableName())) {
            this.joinClauses.add(joinClause);
            this.joinTableNames.add(joinClause.tableName());
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
        this.visitCustomProperty(property.sqlName());
        return null;
    }

    @Override
    public Void visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        this.visitTimeSeries(deliverable.sqlName());
        return null;
    }

    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        this.visitTimeSeries(requirement.sqlName());
        return null;
    }

    @Override
    public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
        unitConversionNode.getExpressionNode().accept(this);
        return null;
    }

    @Override
    public Void visitOperation(OperationNode operationNode) {
        this.visitAll(Arrays.asList(
                operationNode.getLeftOperand(),
                operationNode.getRightOperand()));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode functionCall) {
        this.visitAll(functionCall.getArguments());
        return null;
    }

    private String visitAll(List<ServerExpressionNode> children) {
        children.stream().forEach(child -> child.accept(this));
        return null;
    }

    @Override
    public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return aggregationNode.getAggregatedExpression().accept(this);
    }

    private String timestampFrom(String tableName) {
        return this.fullyQualified(tableName, SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    private String fullyQualified(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    private interface JoinClause {
        String tableName();

        String joinWith(String prefix, String joinTableName);
    }

    private class TimeSeriesJoinClause implements JoinClause {
        private final String tableName;

        private TimeSeriesJoinClause(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public String tableName() {
            return this.tableName;
        }

        @Override
        public String joinWith(String prefix, String joinTableName) {
            return prefix + this.tableName + " ON " + timestampFrom(this.tableName) + " = " + timestampFrom(joinTableName);
        }
    }

    private class PropertiesJoinClause implements JoinClause {
        private final String tableName;

        private PropertiesJoinClause(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public String tableName() {
            return this.tableName;
        }

        @Override
        public String joinWith(String prefix, String joinTableName) {
            return prefix + this.tableName + " ON " + fullyQualified(this.tableName, "starttime") + " < " + timestampFrom(joinTableName)
                    + " AND " + timestampFrom(joinTableName) + " <= " + fullyQualified(this.tableName, "endtime");
        }
    }

}