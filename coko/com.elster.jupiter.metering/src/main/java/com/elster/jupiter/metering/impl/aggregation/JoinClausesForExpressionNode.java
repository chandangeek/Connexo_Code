package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ExpressionNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private String fromTableName;
    private List<String> joinTableNames = new ArrayList<>();

    public JoinClausesForExpressionNode(String joinPrefix) {
        super();
        this.joinPrefix = joinPrefix;
    }

    public List<String> joinClauses() {
        return this.joinTableNames
                .stream()
                .map(this::toJoinClause)
                .collect(Collectors.toList());
    }

    private String toJoinClause(String joinTableName) {
        return this.joinPrefix + joinTableName + " ON " + joinTableName + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName() + " = " + this.fromTableName + "." + SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName();
    }

    private void visitTableName(String tableName) {
        if (this.fromTableName == null) {
            // First encounter, must be the from table
            this.fromTableName = tableName;
        }
        else {
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
    public Void visitVariable(VariableReferenceNode variable) {
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