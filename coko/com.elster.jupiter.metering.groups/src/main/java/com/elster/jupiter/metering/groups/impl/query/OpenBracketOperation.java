package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.util.conditions.Condition;

public final class OpenBracketOperation extends AbstractQueryBuilderOperation {

    public static final OpenBracketOperation OPEN = new OpenBracketOperation();
    public static final String TYPE_IDENTIFIER = "(((";

    static OpenBracketOperation atPosition(int i) {
        OpenBracketOperation openBracketOperation = new OpenBracketOperation();
        openBracketOperation.setPosition(i);
        return openBracketOperation;
    }

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
