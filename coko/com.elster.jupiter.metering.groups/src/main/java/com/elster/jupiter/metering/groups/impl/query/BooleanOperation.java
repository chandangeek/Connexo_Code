package com.elster.jupiter.metering.groups.impl.query;

public abstract class BooleanOperation extends AbstractQueryBuilderOperation {

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitBooleanOperation(this);
    }

    abstract boolean isUnary();
}
