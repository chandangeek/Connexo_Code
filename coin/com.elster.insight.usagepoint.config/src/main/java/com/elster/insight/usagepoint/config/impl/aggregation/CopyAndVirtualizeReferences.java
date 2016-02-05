package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

/**
 * Provides an implementation for the {@link ExpressionNode.Visitor} interface
 * that copies {@link ExpressionNode}s but applies the following replacements:
 * <ul>
 * <li>{@link ReadingTypeRequirementNode} -&gt; {@link VirtualRequirementNode}</li>
 * <li>{@link ReadingTypeDeliverableNode} -&gt; {@link VirtualDeliverableNode}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:04)
 */
class CopyAndVirtualizeReferences implements ExpressionNode.Visitor {

    private final VirtualFactory virtualFactory;
    private final TemporalAmountFactory temporalAmountFactory;
    private final ReadingTypeDeliverable deliverable;
    private final Stack<ExpressionNode> workingCopies = new Stack<>();

    CopyAndVirtualizeReferences(VirtualFactory virtualFactory, TemporalAmountFactory temporalAmountFactory, ReadingTypeDeliverable deliverable) {
        super();
        this.virtualFactory = virtualFactory;
        this.temporalAmountFactory = temporalAmountFactory;
        this.deliverable = deliverable;
    }

    /**
     * Returns the copy of the ExpressionNode.
     * Note that this will throw an IllegalStateException
     * when called during the visit as that would have to
     * return an incomplete and possibly inconsitent copy.
     *
     * @return The copy of the ExpressionNode
     */
    ExpressionNode getCopy() {
        if (this.workingCopies.size() == 1) {
            return this.workingCopies.pop();
        }
        else {
            throw new IllegalStateException("Copying expression node is ongoing");
        }
    }

    @Override
    public void visitConstant(ConstantNode constant) {
        this.workingCopies.push(new ConstantNode(constant.getValue()));
    }

    @Override
    public void visitRequirement(ReadingTypeRequirement requirement) {
        // Replace this one with a VirtualRequirementNode
        this.workingCopies.push(new VirtualRequirementNode(this.virtualFactory, this.temporalAmountFactory, requirement, this.deliverable));
    }

    @Override
    public void visitDeliverable(ReadingTypeDeliverable deliverable) {
        // Replace this one with a VirtualDeliverableNode
        this.workingCopies.push(new VirtualDeliverableNode(this.virtualFactory, this.temporalAmountFactory, deliverable));
    }

    @Override
    public void visitVirtualRequirement(VirtualRequirementNode requirement) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        throw new IllegalArgumentException("Not expecting actual formulas to contain virtual requirement nodes");
    }

    @Override
    public void visitIdentifier(IdentifierNode identifier) {
        this.workingCopies.push(new IdentifierNode(identifier.getName()));
    }

    @Override
    public void visitOperation(OperationNode operationNode) {
        operationNode.getLeft().accept(this);
        operationNode.getRight().accept(this);
        this.workingCopies.push(new OperationNode(operationNode.getOperator(), this.workingCopies.pop(), this.workingCopies.pop()));
    }

    @Override
    public void visitFunctionCall(FunctionCallNode functionCall) {
        functionCall.getIdentifier().accept(this);
        IdentifierNode copiedIdentifier = (IdentifierNode) this.workingCopies.pop();
        functionCall.getArgumentList().accept(this);
        ArgumentListNode copiedArgumentList = (ArgumentListNode) this.workingCopies.pop();
        this.workingCopies.push(new FunctionCallNode(copiedIdentifier, copiedArgumentList));
    }

    @Override
    public void visitArgumentList(ArgumentListNode argumentList) {
        argumentList.getLeft().accept(this);
        ExpressionNode left = this.workingCopies.pop();
        argumentList.getRight().accept(this);
        ExpressionNode right = this.workingCopies.pop();
        this.workingCopies.push(new ArgumentListNode(left, right));
    }

}