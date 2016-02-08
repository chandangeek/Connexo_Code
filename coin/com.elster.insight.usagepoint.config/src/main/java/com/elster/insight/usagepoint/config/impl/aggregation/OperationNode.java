package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode implements ServerExpressionNode {

    private final Operator operator;
    private final ExpressionNode left;
    private final ExpressionNode right;

    public OperationNode(Operator operator, ExpressionNode left, ExpressionNode right) {
        super();
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitOperation(this);
    }

}