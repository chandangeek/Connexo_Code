package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public class OrOperation extends BooleanOperation {

    public static final OrOperation OR = new OrOperation();

    @Override
    public Condition toCondition(Condition... conditions) {
        Condition current = Condition.FALSE;
        for (Condition condition : conditions) {
            current = current.or(condition);
        }
        return current;
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
        return obj != null && OrOperation.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return OrOperation.class.hashCode();
    }

}
