package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNodeImpl;
import com.elster.jupiter.util.units.Dimension;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that copies {@link ExpressionNode}s to the corresponding {@link ServerExpressionNode}
 * and applies the following replacements:
 * <ul>
 * <li>{@link ReadingTypeRequirementNodeImpl} -&gt; {@link VirtualRequirementNode}</li>
 * <li>{@link ReadingTypeDeliverableNodeImpl} -&gt; {@link VirtualDeliverableNode}</li>
 * <li>{@link com.elster.jupiter.metering.impl.config.OperationNodeImpl} whose result
 * is the same or compatible reading type as the deliverable will be
 * replaced with a {@link UnitConversionNode}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class Copy implements ExpressionNode.Visitor<ServerExpressionNode> {

    private final Formula.Mode mode;
    private final VirtualFactory virtualFactory;
    private final ReadingTypeDeliverableForMeterActivationProvider deliverableProvider;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private boolean applyingUnitConversion;

    Copy(Formula.Mode mode, VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivationProvider deliverableProvider, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.mode = mode;
        this.virtualFactory = virtualFactory;
        this.deliverableProvider = deliverableProvider;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
    }

    @Override
    public ServerExpressionNode visitConstant(com.elster.jupiter.metering.config.ConstantNode constant) {
        return new NumericalConstantNode(constant.getValue());
    }

    @Override
    public ServerExpressionNode visitNull(com.elster.jupiter.metering.config.NullNode nullNode) {
        return new NullNode();
    }

    @Override
    public ServerExpressionNode visitRequirement(com.elster.jupiter.metering.config.ReadingTypeRequirementNode node) {
        // Replace this one with a VirtualRequirementNode
        return new VirtualRequirementNode(
                this.mode,
                this.virtualFactory,
                node.getReadingTypeRequirement(),
                this.deliverable,
                this.meterActivation);
    }

    @Override
    public ServerExpressionNode visitDeliverable(com.elster.jupiter.metering.config.ReadingTypeDeliverableNode node) {
        // Replace this one with a VirtualDeliverableNode
        return new VirtualDeliverableNode(
                this.deliverableProvider.from(
                        node.getReadingTypeDeliverable(),
                        this.meterActivation));
    }

    @Override
    public ServerExpressionNode visitOperation(com.elster.jupiter.metering.config.OperationNode operationNode) {
        if (this.applyingUnitConversion || Formula.Mode.EXPERT.equals(this.mode)) {
            return this.simpleCopy(operationNode);
        } else {
            Optional<Dimension> dimension = this.getIntermediateDimension(operationNode).getDimension();
            VirtualReadingType targetReadingType = VirtualReadingType.from(this.deliverable.getReadingType());
            if (dimension.isPresent()) {
                if (dimension.get().isDimensionLess()) {
                    /* All operands are dimensionless which is always compatible for automatic unit conversion
                     * with the deliverable's reading type but it's not that type of expressions
                     * that we want to wrap in a UnitConversionNode. */
                    return this.simpleCopy(operationNode);
                } else if (this.necessitatesUnitConversionNode(operationNode, dimension.get())) {
                    /* Gotcha: found an operation that matches (modulo unit conversion)
                     * the reading type of the deliverable, replace it with a UnitConversionNode. */
                    this.applyingUnitConversion = true;
                    UnitConversionNode copied = new UnitConversionNode(this.simpleCopy(operationNode), dimension.get(), targetReadingType);
                    this.applyingUnitConversion = false;
                    return copied;
                }
            }
            return this.simpleCopy(operationNode);
        }
    }

    private boolean necessitatesUnitConversionNode(com.elster.jupiter.metering.config.Operator operator) {
        return EnumSet.of(
                    com.elster.jupiter.metering.config.Operator.MULTIPLY,
                    com.elster.jupiter.metering.config.Operator.SAFE_DIVIDE,
                    com.elster.jupiter.metering.config.Operator.DIVIDE)
                .contains(operator);
    }

    private boolean necessitatesUnitConversionNode(com.elster.jupiter.metering.config.OperationNode operationNode, Dimension dimension) {
        return EnumSet.of(
                com.elster.jupiter.metering.config.Operator.MULTIPLY,
                com.elster.jupiter.metering.config.Operator.SAFE_DIVIDE,
                com.elster.jupiter.metering.config.Operator.DIVIDE).contains(operationNode.getOperator())
            && UnitConversionSupport.areCompatibleForAutomaticUnitConversion(dimension, this.deliverable.getReadingType().getUnit().getUnit().getDimension());
    }

    private ServerExpressionNode simpleCopy(com.elster.jupiter.metering.config.OperationNode operationNode) {
        Operator operator = Operator.from(operationNode.getOperator());
        ServerExpressionNode operand1 = operationNode.getLeftOperand().accept(this);
        ServerExpressionNode operand2 = operationNode.getRightOperand().accept(this);
        if (com.elster.jupiter.metering.config.Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            return new OperationNode(
                    operator,
                    operand1,
                    operand2,
                    operationNode.getZeroReplacement().get().accept(this));
        } else {
            return new OperationNode(
                    operator,
                    operand1,
                    operand2);
        }
    }

    private IntermediateDimension getIntermediateDimension(com.elster.jupiter.metering.config.ExpressionNode node) {
        return ((com.elster.jupiter.metering.impl.config.ServerExpressionNode) node).getIntermediateDimension();
    }

    @Override
    public ServerExpressionNode visitFunctionCall(com.elster.jupiter.metering.config.FunctionCallNode functionCall) {
        List<ServerExpressionNode> arguments = functionCall.getChildren().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        Function function = Function.from(functionCall.getFunction());
        switch (functionCall.getFunction()) {
            case AGG_TIME: {
                if (arguments.size() != 1) {
                    throw new IllegalArgumentException("Time based aggregation only supports 1 argument");
                }
                return new TimeBasedAggregationNode(arguments.get(0), VirtualReadingType.from(this.deliverable.getReadingType()));
            }
            case SUM:   // Intentional fall-through
            case AVG:   // Intentional fall-through
            case MIN_AGG:// Intentional fall-through
            case MAX_AGG: {
                if (arguments.size() != 1) {
                    throw new IllegalArgumentException("Time based aggregation only supports 1 argument");
                }
                return new TimeBasedAggregationNode(
                        arguments.get(0),
                        AggregationFunction.from(functionCall.getFunction()),
                        IntervalLength.from(functionCall.getAggregationLevel().get()));
            }
            case MAX:   // Intentional fall-through
            case MIN:   // Intentional fall-through
            case POWER: // Intentional fall-through
            case SQRT:  // Intentional fall-through
            case FIRST_NOT_NULL: {
                return new FunctionCallNode(function, arguments);
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported function when copying expression nodes " + function.name());
            }
        }
    }

}