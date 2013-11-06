package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public interface QueryBuilderOperation {

    void visit(OperationVisitor visitor);

    Condition toCondition(Condition... conditions);
}
