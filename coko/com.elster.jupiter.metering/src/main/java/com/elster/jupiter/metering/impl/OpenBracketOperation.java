package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public final class OpenBracketOperation implements QueryBuilderOperation {

    public static final OpenBracketOperation OPEN = new OpenBracketOperation();

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitOpenBracketOperation(this);
    }

    @Override
    public Condition toCondition(Condition... conditions) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && OpenBracketOperation.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return OpenBracketOperation.class.hashCode();
    }
}
