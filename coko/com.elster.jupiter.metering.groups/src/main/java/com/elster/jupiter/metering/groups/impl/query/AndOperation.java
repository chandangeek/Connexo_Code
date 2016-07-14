package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.util.conditions.Condition;

public class AndOperation extends BooleanOperation {

    public static final AndOperation AND = new AndOperation();
    public static final String TYPE_IDENTIFIER = "AND";

    static AndOperation atPosition(int i) {
        AndOperation andOperation = new AndOperation();
        andOperation.setPosition(i);
        return andOperation;
    }

    @Override
    public Condition toCondition(Condition... conditions) {
        Condition result = Condition.TRUE;
        for (Condition condition : conditions) {
            result = result.and(condition);
        }
        return result;
    }

    @Override
    boolean isUnary() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && AndOperation.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return AndOperation.class.hashCode();
    }

}
