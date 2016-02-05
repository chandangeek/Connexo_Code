package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode implements ExpressionNode {

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
    public void accept(Visitor visitor) {
        visitor.visitOperation(this);
    }

}