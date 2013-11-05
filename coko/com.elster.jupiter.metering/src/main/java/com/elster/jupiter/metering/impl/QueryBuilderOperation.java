package com.elster.jupiter.metering.impl;

public interface QueryBuilderOperation {

    void visit(OperationVisitor visitor);
}
