package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class ArgumentListNode implements ServerExpressionNode {

    private final ExpressionNode left;
    private final ExpressionNode right;

    public ArgumentListNode(ExpressionNode left, ExpressionNode right) {
        super();
        this.left = left;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArgumentList(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitArgumentList(this);
    }

}