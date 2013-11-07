package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.collections.ArrayStack;
import com.elster.jupiter.util.collections.Stack;
import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Constant;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Exists;
import com.elster.jupiter.util.conditions.FragmentExpression;
import com.elster.jupiter.util.conditions.Membership;
import com.elster.jupiter.util.conditions.Not;
import com.elster.jupiter.util.conditions.Or;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    private final List<QueryBuilderOperation> operations = new ArrayList<>();

    public static QueryBuilder parse(Condition condition) {
        QueryBuilder queryBuilder = new QueryBuilder();
        condition.visit(queryBuilder.new QueryBuilderVisitor());
        return queryBuilder;
    }

    public Condition toCondition() {
        ConditionBuilderVisitor conditionBuilderVisitor = new ConditionBuilderVisitor();
        for (QueryBuilderOperation operation : operations) {
            operation.visit(conditionBuilderVisitor);
        }
        return conditionBuilderVisitor.current.pop();
    }

    private class ConditionBuilderVisitor implements OperationVisitor {

        private final Stack<Condition> current = new ArrayStack<>();
        private final Stack<QueryBuilderOperation> stack = new ArrayStack<>();

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
    }

    private class QueryBuilderVisitor implements Visitor {

        @Override
        public void visitOr(Or or) {
            add(OpenBracketOperation.atPosition(operations.size()));
            or.getConditions().get(0).visit(this);
            add(OrOperation.atPosition(operations.size()));
            or.getConditions().get(1).visit(this);
            add(CloseBracketOperation.atPosition(operations.size()));
        }

        @Override
        public void visitAnd(And and) {
            add(OpenBracketOperation.atPosition(operations.size()));
            and.getConditions().get(0).visit(this);
            add(AndOperation.atPosition(operations.size()));
            and.getConditions().get(1).visit(this);
            add(CloseBracketOperation.atPosition(operations.size()));
        }

        @Override
        public void visitComparison(Comparison comparison) {
            SimpleConditionOperation operation = new SimpleConditionOperation(comparison);
            operation.setPosition(operations.size());
            add(operation);
        }

        @Override
        public void visitNot(Not not) {
            add(NotOperation.atPosition(operations.size()));
            add(OpenBracketOperation.atPosition(operations.size()));
            not.getNegated().visit(this);
            add(CloseBracketOperation.atPosition(operations.size()));
        }

        @Override
        public void visitTrue(Constant trueCondition) {
            throw new UnsupportedOperationException("True constant not supported.");
        }

        @Override
        public void visitFalse(Constant falseCondition) {
            throw new UnsupportedOperationException("False constant not supported.");
        }

        @Override
        public void visitContains(Contains contains) {
            throw new UnsupportedOperationException("Contains not supported.");
        }

        @Override
        public void visitMembership(Membership member) {
            throw new UnsupportedOperationException("Membership not supported.");
        }

        @Override
        public void visitExists(Exists empty) {
            throw new UnsupportedOperationException("Exists not supported.");
        }

        @Override
        public void visitText(Text expression) {
            throw new UnsupportedOperationException("Text not supported.");
        }

        @Override
        public void visitFragmentExpression(FragmentExpression expression) {
            throw new UnsupportedOperationException("Fragment expressions not supported.");
        }
    }

    private void add(QueryBuilderOperation operation) {
        operations.add(operation);
    }
}
