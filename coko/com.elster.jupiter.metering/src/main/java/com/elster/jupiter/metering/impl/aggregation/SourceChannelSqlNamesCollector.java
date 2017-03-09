package com.elster.jupiter.metering.impl.aggregation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class SourceChannelSqlNamesCollector implements ServerExpressionNode.Visitor<String> {

    private final Set<String> sourceChannelSqlNames = new HashSet<>();

    Set<String> getSourceChannelSqlNames() {
        return Collections.unmodifiableSet(this.sourceChannelSqlNames);
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
        sourceChannelSqlNames.add(requirement.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
        return null;
    }

    @Override
    public String visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        sourceChannelSqlNames.add(deliverable.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
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
