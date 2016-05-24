package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.Arrays;
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
public class JoinClausesForExpressionNode implements ServerExpressionNode.Visitor<Void> {

    private final String joinPrefix;
    private String sourceTableName;
    private Set<String> joinTableNames = new HashSet<>();

    public JoinClausesForExpressionNode(String joinPrefix, String sourceTableName) {
        super();
        this.joinPrefix = joinPrefix;
        this.sourceTableName = sourceTableName;
    }

    public List<String> joinClauses() {
        return this.joinTableNames
                .stream()
                .map(this::toJoinClause)
                .collect(Collectors.toList());
    }

    private String toJoinClause(String joinTableName) {
        return this.joinPrefix + joinTableName + " ON " + joinTableName + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName() + " = " + this.sourceTableName + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName();
    }

    private void visitTableName(String tableName) {
        if (!this.sourceTableName.equals(tableName)) {
            this.joinTableNames.add(tableName);
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
    public Void visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public Void visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        this.visitTableName(deliverable.sqlName());
        return null;
    }

    @Override
    public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
        this.visitTableName(requirement.sqlName());
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

}