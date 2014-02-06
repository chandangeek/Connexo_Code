package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.impl.query.OperationVisitor;
import com.elster.jupiter.util.conditions.Condition;

public interface QueryBuilderOperation {

    void visit(OperationVisitor visitor);

    void setPosition(int i);

    Condition toCondition(Condition... conditions);

    void setGroupId(long id);
}
