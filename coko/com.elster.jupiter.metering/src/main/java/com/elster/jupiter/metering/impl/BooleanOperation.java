package com.elster.jupiter.metering.impl;

public abstract class BooleanOperation implements QueryBuilderOperation {

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitBooleanOperation(this);
    }

    abstract boolean isUnary();
}
