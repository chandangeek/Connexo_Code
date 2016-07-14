package com.elster.jupiter.metering.groups.impl.query;

abstract class BooleanOperation extends AbstractQueryBuilderOperation {

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitBooleanOperation(this);
    }

    abstract boolean isUnary();

}