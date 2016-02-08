package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode implements ServerExpressionNode {

    private final IdentifierNode identifier;
    private final ArgumentListNode argumentList;

    public FunctionCallNode(IdentifierNode identifier, ArgumentListNode argumentList) {
        super();
        this.identifier = identifier;
        this.argumentList = argumentList;
    }

    public IdentifierNode getIdentifier() {
        return identifier;
    }

    public ArgumentListNode getArgumentList() {
        return argumentList;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

}