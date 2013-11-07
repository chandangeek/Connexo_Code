package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public interface QueryBuilderOperation {

    void visit(OperationVisitor visitor);

    void setPosition(int i);

    Condition toCondition(Condition... conditions);
}
