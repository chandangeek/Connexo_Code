package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.util.conditions.Condition;

/**
 * All instances are regarded to be equal.
 */
public final class NotOperation extends BooleanOperation {

    public static final NotOperation NOT = new NotOperation();
    public static final String TYPE_IDENTIFIER = "NOT";

    static NotOperation atPosition(int i) {
        NotOperation notOperation = new NotOperation();
        notOperation.setPosition(i);
        return notOperation;
    }

    @Override
    public Condition toCondition(Condition... conditions) {
        return conditions[0].not();
    }

    @Override
    boolean isUnary() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && NotOperation.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return NotOperation.class.hashCode();
    }
}
