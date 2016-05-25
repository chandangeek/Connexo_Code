package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.CustomPropertyNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNodeImpl;
import com.elster.jupiter.util.units.Dimension;

import java.util.List;
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
    public ServerExpressionNode visitProperty(CustomPropertyNode property) {
        throw new UnsupportedOperationException("Todo: complete me");
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
        Operator operator = Operator.from(operationNode.getOperator());
        ServerExpressionNode operand1 = operationNode.getLeftOperand().accept(this);
        ServerExpressionNode operand2 = operationNode.getRightOperand().accept(this);
        if (com.elster.jupiter.metering.config.Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            return new OperationNode(
                    operator,
                    this.getIntermediateDimension(operationNode),
                    operand1,
                    operand2,
                    operationNode.getZeroReplacement().get().accept(this));
        } else {
            return new OperationNode(
                    operator,
                    this.getIntermediateDimension(operationNode),
                    operand1,
                    operand2);
        }
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
                return new FunctionCallNode(function, IntermediateDimension.of(Dimension.DIMENSIONLESS), arguments);
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported function when copying expression nodes " + function.name());
            }
        }
    }

    private IntermediateDimension getIntermediateDimension(com.elster.jupiter.metering.config.ExpressionNode node) {
        return ((com.elster.jupiter.metering.impl.config.ServerExpressionNode) node).getIntermediateDimension();
    }

}