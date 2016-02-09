package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerExpressionNode.ServerVisitor} interface
 * that copies {@link ExpressionNode}s but applies the following replacements:
 * <ul>
 * <li>{@link ReadingTypeRequirementNode} -&gt; {@link VirtualRequirementNode}</li>
 * <li>{@link ReadingTypeDeliverableNode} -&gt; {@link VirtualDeliverableNode}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class CopyAndVirtualizeReferences implements ServerExpressionNode.ServerVisitor<AbstractNode> {

    private final VirtualFactory virtualFactory;
    private final TemporalAmountFactory temporalAmountFactory;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;

    CopyAndVirtualizeReferences(VirtualFactory virtualFactory, TemporalAmountFactory temporalAmountFactory, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.virtualFactory = virtualFactory;
        this.temporalAmountFactory = temporalAmountFactory;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
    }

    @Override
    public AbstractNode visitConstant(ConstantNode constant) {
        return new ConstantNode(constant.getValue());
    }

    @Override
    public AbstractNode visitRequirement(ReadingTypeRequirementNode node) {
        // Replace this one with a VirtualRequirementNode
        return new VirtualRequirementNode(
                this.virtualFactory,
                node.getReadingTypeRequirement(),
                this.deliverable,
                this.meterActivation);
    }

    @Override
    public AbstractNode visitDeliverable(ReadingTypeDeliverableNode node) {
        // Replace this one with a VirtualDeliverableNode
        return new VirtualDeliverableNode(this.virtualFactory, node.getReadingTypeDeliverable());
    }

    @Override
    public AbstractNode visitVirtualRequirement(VirtualRequirementNode requirement) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public AbstractNode visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public AbstractNode visitOperation(OperationNode operationNode) {
        return new OperationNode(
                operationNode.getOperator(),
                operationNode.getLeftOperand().accept(this),
                operationNode.getRightOperand().accept(this));
    }

    @Override
    public AbstractNode visitFunctionCall(FunctionCallNode functionCall) {
        List<AbstractNode> arguments = functionCall.getChildren().stream().map(child -> child.accept(this)).collect(Collectors.toList());
        Function function = functionCall.getFunction();
        if (function == null) {
            return new FunctionCallNode(arguments, functionCall.getName());
        }
        else {
            return new FunctionCallNode(arguments, function);
        }
    }

}