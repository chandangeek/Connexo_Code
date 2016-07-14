package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.metering.groups.impl.QueryBuilderOperation;
import com.elster.jupiter.util.conditions.Condition;

import java.util.ArrayDeque;
import java.util.Deque;

class ConditionBuilderVisitor implements OperationVisitor {

    private final Deque<Condition> current = new ArrayDeque<>();
    private final Deque<QueryBuilderOperation> stack = new ArrayDeque<>();

    @Override
    public void visitBooleanOperation(BooleanOperation booleanOperation) {
        stack.push(booleanOperation);
    }

    @Override
    public void visitCloseBracketOperation(CloseBracketOperation bracketOperation) {
        QueryBuilderOperation queryBuilderOperation = stack.pop();
        if (queryBuilderOperation instanceof BooleanOperation) {
            stack.pop();
            Condition last = current.pop();
            current.push(queryBuilderOperation.toCondition(current.pop(), last));
        }
        if (!stack.isEmpty() && NotOperation.NOT.equals(stack.peek())) {
            stack.pop();
            current.push(NotOperation.NOT.toCondition(current.pop()));
        }
    }

    @Override
    public void visitOpenBracketOperation(OpenBracketOperation bracketOperation) {
        stack.push(bracketOperation);
    }

    @Override
    public void visitSimpleCondition(SimpleConditionOperation simpleConditionOperation) {
        if (current == null) {
            current.push(simpleConditionOperation.toCondition());
        } else {
            if (!stack.isEmpty() && stack.peek() instanceof BooleanOperation) {
                BooleanOperation booleanOperation = (BooleanOperation) stack.pop();
                current.push(booleanOperation.toCondition(current.pop(), simpleConditionOperation.toCondition()));
            } else {
                current.push(simpleConditionOperation.toCondition());
            }
        }
    }

    Condition pop() {
        return (current.isEmpty() ? Condition.TRUE : current.pop());
    }

}