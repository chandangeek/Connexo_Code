package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNodeImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that copies {@link ExpressionNode}s to the corresponding {@link ServerExpressionNode}
 * and applies the following replacements:
 * <ul>
 * <li>{@link ReadingTypeRequirementNodeImpl} -&gt; {@link VirtualRequirementNode}</li>
 * <li>{@link ReadingTypeDeliverableNodeImpl} -&gt; {@link VirtualDeliverableNode}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class CopyAndVirtualizeReferences implements ExpressionNode.Visitor<ServerExpressionNode> {

    private final Formula.Mode mode;
    private final VirtualFactory virtualFactory;
    private final ReadingTypeDeliverableForMeterActivationProvider deliverableProvider;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;

    CopyAndVirtualizeReferences(Formula.Mode mode, VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivationProvider deliverableProvider, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
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
        return new NullNodeImpl();
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
                this.virtualFactory,
                this.deliverableProvider.from(
                        node.getReadingTypeDeliverable(),
                        this.meterActivation));
    }

    @Override
    public ServerExpressionNode visitOperation(com.elster.jupiter.metering.config.OperationNode operationNode) {
        return new OperationNode(
                Operator.from(operationNode.getOperator()),
                operationNode.getLeftOperand().accept(this),
                operationNode.getRightOperand().accept(this));
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
            case SUM:// Intentional fall-through
            case AVG:// Intentional fall-through
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
            default: {
                return new FunctionCallNode(function, arguments);
            }
        }
    }

}