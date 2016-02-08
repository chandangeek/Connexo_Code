package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

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
class CopyAndVirtualizeReferences implements ServerExpressionNode.ServerVisitor<ServerExpressionNode> {

    private final VirtualFactory virtualFactory;
    private final TemporalAmountFactory temporalAmountFactory;
    private final ReadingTypeDeliverable deliverable;

    CopyAndVirtualizeReferences(VirtualFactory virtualFactory, TemporalAmountFactory temporalAmountFactory, ReadingTypeDeliverable deliverable) {
        super();
        this.virtualFactory = virtualFactory;
        this.temporalAmountFactory = temporalAmountFactory;
        this.deliverable = deliverable;
    }

    @Override
    public ServerExpressionNode visitConstant(ConstantNode constant) {
        return new ConstantNode(constant.getValue());
    }

    @Override
    public ServerExpressionNode visitRequirement(ReadingTypeRequirementNode node) {
        // Replace this one with a VirtualRequirementNode
        return new VirtualRequirementNode(this.virtualFactory, this.temporalAmountFactory, node.getReadingTypeRequirement(), this.deliverable);
    }

    @Override
    public ServerExpressionNode visitDeliverable(ReadingTypeDeliverableNode node) {
        // Replace this one with a VirtualDeliverableNode
        return new VirtualDeliverableNode(this.virtualFactory, this.temporalAmountFactory, node.getReadingTypeDeliverable());
    }

    @Override
    public ServerExpressionNode visitVirtualRequirement(VirtualRequirementNode requirement) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public ServerExpressionNode visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public ServerExpressionNode visitIdentifier(IdentifierNode identifier) {
        return new IdentifierNode(identifier.getName());
    }

    @Override
    public ServerExpressionNode visitOperation(OperationNode operationNode) {
        return new OperationNode(
                operationNode.getOperator(),
                operationNode.getLeft().accept(this),
                operationNode.getRight().accept(this));
    }

    @Override
    public ServerExpressionNode visitFunctionCall(FunctionCallNode functionCall) {
        return new FunctionCallNode(
                (IdentifierNode) functionCall.getIdentifier().accept(this),
                (ArgumentListNode) functionCall.getArgumentList().accept(this));
    }

    @Override
    public ServerExpressionNode visitArgumentList(ArgumentListNode argumentList) {
        return new ArgumentListNode(
                argumentList.getLeft().accept(this),
                argumentList.getRight().accept(this));
    }

}