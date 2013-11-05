package com.elster.jupiter.metering.impl;

public enum BracketOperation implements QueryBuilderOperation {
    OPEN, CLOSE;

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitBracketOperation(this);
    }


}
