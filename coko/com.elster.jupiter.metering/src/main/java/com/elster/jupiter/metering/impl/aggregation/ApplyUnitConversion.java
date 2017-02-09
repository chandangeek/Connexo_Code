/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.util.units.Dimension;
import com.elster.jupiter.util.units.Unit;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerExpressionNode.Visitor} interface
 * that checks for situations where a {@link UnitConversionNode} is necessary.
 * Note that this cannot be done in the {@link Copy} visitor as that one is working
 * on {@link com.elster.jupiter.metering.config.ExpressionNode}s that do
 * not have the capability to provide preferred reading types because
 * they do not know the meters that are backing the requested data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-03 (15:02)
 */
class ApplyUnitConversion implements ServerExpressionNode.Visitor<ServerExpressionNode> {

    private boolean applyingUnitConversion;
    private final Formula.Mode mode;
    private final VirtualReadingType targetReadingType;

    ApplyUnitConversion(Formula.Mode mode, VirtualReadingType targetReadingType) {
        this.mode = mode;
        this.targetReadingType = targetReadingType;
        this.applyingUnitConversion = false;
    }

    @Override
    public ServerExpressionNode visitConstant(NumericalConstantNode constant) {
        // No replacement
        return constant;
    }

    @Override
    public ServerExpressionNode visitConstant(StringConstantNode constant) {
        // No replacement
        return constant;
    }

    @Override
    public ServerExpressionNode visitProperty(CustomPropertyNode property) {
        // No replacement
        return property;
    }

    @Override
    public ServerExpressionNode visitNull(NullNode nullNode) {
        // No replacement
        return nullNode;
    }

    @Override
    public ServerExpressionNode visitSqlFragment(SqlFragmentNode variable) {
        // No replacement
        return variable;
    }

    @Override
    public ServerExpressionNode visitVirtualRequirement(VirtualRequirementNode requirement) {
        // No replacement
        return requirement;
    }

    @Override
    public ServerExpressionNode visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // No replacement
        return deliverable;
    }

    @Override
    public ServerExpressionNode visitUnitConversion(UnitConversionNode unitConversionNode) {
        throw new IllegalStateException("Not expecting any UnitConversionNodes as only this component is responsible for creating them");
    }

    @Override
    public ServerExpressionNode visitFunctionCall(FunctionCallNode functionCall) {
        // Copy as child nodes may be replaced
        List<ServerExpressionNode> arguments = functionCall.getArguments().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        return new FunctionCallNode(functionCall.getFunction(), IntermediateDimension.of(Dimension.DIMENSIONLESS), arguments);
    }

    @Override
    public ServerExpressionNode visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        return new TimeBasedAggregationNode(
                aggregationNode.getAggregatedExpression().accept(this),
                aggregationNode.getFunction(),
                aggregationNode.getIntervalLength());
    }

    @Override
    public ServerExpressionNode visitOperation(OperationNode operationNode) {
        if (this.applyingUnitConversion || Formula.Mode.EXPERT.equals(this.mode)) {
            return this.simpleCopy(operationNode);
        } else {
            if (this.necessitatesUnitConversionNode(operationNode)) {
                Optional<Dimension> dimension = operationNode.getIntermediateDimension().getDimension();
                if (dimension.isPresent()) {
                    this.applyingUnitConversion = true;
                    UnitConversionNode copied = new UnitConversionNode(this.simpleCopy(operationNode), dimension.get(), this.targetReadingType);
                    this.applyingUnitConversion = false;
                    return copied;
                } else {
                    // Sorry cannot apply UnitConversionNode after all
                    return this.simpleCopy(operationNode);
                }
            } else {
                return this.simpleCopy(operationNode);
            }
        }
    }

    private boolean necessitatesUnitConversionNode(OperationNode operationNode) {
        if (this.necessitatesUnitConversionNode(operationNode.getOperator())) {
            CollectSourceReadingTypes sourceReadingTypesCollector = new CollectSourceReadingTypes();
            operationNode.accept(sourceReadingTypesCollector);
            return this.necessitatesUnitConversionNode(sourceReadingTypesCollector.getReadingTypes());
        } else {
            return false;
        }
    }

    private boolean necessitatesUnitConversionNode(Collection<VirtualReadingType> sourceReadingTypes) {
        if (sourceReadingTypes.isEmpty()) {
            // Only constants or nodes that do not require unit conversion
            return false;
        }
        Set<MetricMultiplier> unitMultipliers =
                sourceReadingTypes
                        .stream()
                        .map(VirtualReadingType::getUnitMultiplier)
                        .collect(Collectors.toSet());
        if (unitMultipliers.size() > 1) {
            /* At least two different source unit multipliers so at least
             * one node will need unit multiplier rescaling in combination
             * with a dangerous numerical operation and that is a recipe for disaster. */
            return true;
        } else {
            // Number of unit multipliers must be 1 since number of source reading types > 0
            if (unitMultipliers.iterator().next().equals(this.targetReadingType.getUnitMultiplier())) {
                return false;
            } else {
                return sourceReadingTypes
                        .stream()
                        .map(VirtualReadingType::getUnit)
                        .map(ReadingTypeUnit::getUnit)
                        .map(Unit::getDimension)
                        .allMatch(this::supportsRescaling);
            }
        }
    }

    private boolean necessitatesUnitConversionNode(Operator operator) {
        return EnumSet.of(Operator.MULTIPLY, Operator.SAFE_DIVIDE, Operator.DIVIDE).contains(operator);
    }

    private boolean supportsRescaling(Dimension dimension) {
        Set<Dimension> unsupportive =
                EnumSet.of(
                        Dimension.DIMENSIONLESS,
                        Dimension.TIME,
                        Dimension.TEMPERATURE,
                        Dimension.FUEL_EFFICIENCY,
                        Dimension.FUEL_ECONOMY);
        return !unsupportive.contains(dimension);
    }

    private ServerExpressionNode simpleCopy(OperationNode operationNode) {
        Operator operator = operationNode.getOperator();
        ServerExpressionNode operand1 = operationNode.getLeftOperand().accept(this);
        ServerExpressionNode operand2 = operationNode.getRightOperand().accept(this);
        if (Operator.SAFE_DIVIDE.equals(operator)) {
            return new OperationNode(
                    operator,
                    operationNode.getIntermediateDimension(),
                    operand1,
                    operand2,
                    operationNode.getSafeDivisor().accept(this));
        } else {
            return new OperationNode(
                    operator,
                    operationNode.getIntermediateDimension(),
                    operand1,
                    operand2);
        }
    }

}