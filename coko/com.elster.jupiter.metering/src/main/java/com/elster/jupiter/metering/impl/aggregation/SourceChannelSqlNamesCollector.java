package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class SourceChannelSqlNamesCollector implements ServerExpressionNode.Visitor<String> {

    private final Set<String> sourceChannelSqlNames = new HashSet<>();
    private final boolean referenceMode;

    Set<String> getSourceChannelSqlNames() {
        return Collections.unmodifiableSet(this.sourceChannelSqlNames);
    }

    static void appendTo(SqlBuilder sqlBuilder, ServerExpressionNode expressionNode) {
        appendTo(sqlBuilder, expressionNode, new SourceChannelSqlNamesCollector(true));
    }

    static void appendLeafsTo(SqlBuilder sqlBuilder, ServerExpressionNode expressionNode) {
        appendTo(sqlBuilder, expressionNode, new SourceChannelSqlNamesCollector(false));
    }

    SourceChannelSqlNamesCollector(boolean referenceMode) {
        super();
        this.referenceMode = referenceMode;
    }

    private static void appendTo(SqlBuilder sqlBuilder, ServerExpressionNode expressionNode, SourceChannelSqlNamesCollector collector) {
        expressionNode.accept(collector);
        String channels = SourceChannelSetFactory.format(collector.getSourceChannelSqlNames());
        if (channels.isEmpty()) {
            sqlBuilder.append("''");
        } else {
            sqlBuilder.append(channels);
        }
    }

    @Override
    public String visitNull(NullNode nullNode) {
        return null;
    }

    @Override
    public String visitConstant(NumericalConstantNode constant) {
        return null;
    }

    @Override
    public String visitConstant(StringConstantNode constant) {
        return null;
    }

    @Override
    public String visitProperty(CustomPropertyNode property) {
        return null;
    }

    @Override
    public String visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return null;
    }

    @Override
    public String visitSqlFragment(SqlFragmentNode variable) {
        return null;
    }

    @Override
    public String visitVirtualRequirement(VirtualRequirementNode requirement) {
        if (this.referenceMode) {
            this.sourceChannelSqlNames.add(requirement.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
        } else {
            this.sourceChannelSqlNames.add(requirement.sourceChannelValue());
        }
        return null;
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        if (this.referenceMode) {
            this.sourceChannelSqlNames.add(deliverable.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
        } else {
            this.sourceChannelSqlNames.addAll(deliverable.sourceChannelValues());
        }
        return null;
    }

    @Override
    public String visitUnitConversion(UnitConversionNode unitConversionNode) {
        return null;
    }

    @Override
    public String visitOperation(OperationNode operationNode) {
        operationNode.getLeftOperand().accept(this);
        operationNode.getRightOperand().accept(this);
        if (Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            operationNode.getSafeDivisor().accept(this);
        }
        return null;
    }

    @Override
    public String visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getArguments().forEach(arg -> arg.accept(this));
        return null;
    }

    @Override
    public String visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        aggregationNode.getAggregatedExpression().accept(this);
        return null;
    }

}