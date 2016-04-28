package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNodeImpl;
import com.elster.jupiter.util.units.Dimension;

import java.util.Deque;
import java.util.LinkedList;
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
    private Deque<Boolean> alreadyConverted = new LinkedList<>();

    Copy(Formula.Mode mode, VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivationProvider deliverableProvider, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.mode = mode;
        this.virtualFactory = virtualFactory;
        this.deliverableProvider = deliverableProvider;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
    }

    private void didNotApplyUnitConversion() {
        this.alreadyConverted.push(false);
    }

    private ServerExpressionNode didNotApplyUnitConversion(ServerExpressionNode result) {
        this.didNotApplyUnitConversion();
        return result;
    }

    private ServerExpressionNode appliedUnitConversion(ServerExpressionNode result) {
        this.alreadyConverted.push(true);
        return result;
    }

    @Override
    public ServerExpressionNode visitConstant(com.elster.jupiter.metering.config.ConstantNode constant) {
        this.didNotApplyUnitConversion();
        return new NumericalConstantNode(constant.getValue());
    }

    @Override
    public ServerExpressionNode visitNull(com.elster.jupiter.metering.config.NullNode nullNode) {
        this.didNotApplyUnitConversion();
        return new NullNode();
    }

    @Override
    public ServerExpressionNode visitRequirement(com.elster.jupiter.metering.config.ReadingTypeRequirementNode node) {
        this.didNotApplyUnitConversion();
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
        this.didNotApplyUnitConversion();
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
        boolean leftIsAlreadyConverted = this.alreadyConverted.pop();
        ServerExpressionNode operand2 = operationNode.getRightOperand().accept(this);
        boolean rightIsAlreadyConverted = this.alreadyConverted.pop();
        OperationNode copied;
        if (com.elster.jupiter.metering.config.Operator.SAFE_DIVIDE.equals(operationNode.getOperator())) {
            copied = new OperationNode(
                    operator,
                    operand1,
                    operand2,
                    operationNode.getZeroReplacement().get().accept(this));
            return copied;
        } else {
            copied = new OperationNode(
                    operator,
                    operand1,
                    operand2);
        }
        if (leftIsAlreadyConverted || rightIsAlreadyConverted) {
            /* No need to wrap it in a UnitConversionNode again to avoid
             * that sql generation would apply unit conversion twice.
             * Consider the example of multiplying the expression,
             * that is already wrapped in a UnitConversionNode,
             * with a constant. The dimension of the result of that
             * multiplication is compatible for unit conversion
             * so without this test, it would be wrapped again. */
            return this.appliedUnitConversion(copied);
        } else {
            Optional<Dimension> dimension = this.getIntermediateDimension(operationNode).getDimension();
            if (dimension.isPresent()) {
                if (dimension.get().isDimensionLess()) {
                    /* All operands are dimension less which is always compatible for automatic unit conversion
                     * with the deliverable's reading type but it's not that type of expressions
                     * that we want to wrap in a UnitConversionNode. */
                    return this.didNotApplyUnitConversion(copied);
                } else if (UnitConversionSupport.areCompatibleForAutomaticUnitConversion(dimension.get(), this.deliverable.getReadingType().getUnit().getUnit().getDimension())) {
                    /* Gotcha: found an operation that matches (modulo unit conversion)
                     * the reading type of the deliverable, replace it with a UnitConversionNode. */
                    return this.appliedUnitConversion(new UnitConversionNode(copied, VirtualReadingType.from(this.deliverable.getReadingType())));
                }
            }
        }
        return this.didNotApplyUnitConversion(copied);
    }

    private IntermediateDimension getIntermediateDimension(com.elster.jupiter.metering.config.OperationNode operationNode) {
        return ((com.elster.jupiter.metering.impl.config.ServerExpressionNode) operationNode).getIntermediateDimension();
    }

    @Override
    public ServerExpressionNode visitFunctionCall(com.elster.jupiter.metering.config.FunctionCallNode functionCall) {
        this.didNotApplyUnitConversion();
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