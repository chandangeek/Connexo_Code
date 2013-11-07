package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public class CloseBracketOperation extends AbstractQueryBuilderOperation {

    public static final CloseBracketOperation CLOSE = new CloseBracketOperation();
    static final String TYPE_IDENTIFIER = " ) ";

    public static CloseBracketOperation atPosition(int i) {
        CloseBracketOperation closeBracketOperation = new CloseBracketOperation();
        closeBracketOperation.setPosition(i);
        return closeBracketOperation;
    }

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitCloseBracketOperation(this);
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
        return obj != null && CloseBracketOperation.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return CloseBracketOperation.class.hashCode();
    }

}
