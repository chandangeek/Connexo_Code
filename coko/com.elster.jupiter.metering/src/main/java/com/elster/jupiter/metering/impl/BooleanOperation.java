package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Condition;

public enum BooleanOperation implements QueryBuilderOperation {

    NOT {
        @Override
        public Condition toCondition(Condition... conditions) {
            return conditions[0].not();
        }
    },
    AND {
        @Override
        public Condition toCondition(Condition... conditions) {
            Condition current = Condition.TRUE;
            for (Condition condition : conditions) {
                current = current.and(condition);
            }
            return current;
        }
    },
    OR {
        @Override
        public Condition toCondition(Condition... conditions) {
            Condition current = Condition.FALSE;
            for (Condition condition : conditions) {
                current = current.or(condition);
            }
            return current;
        }
    };

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitBooleanOperation(this);
    }

    public abstract Condition toCondition(Condition... conditions);
}
