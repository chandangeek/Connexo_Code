package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.metering.groups.impl.QueryBuilderOperation;
import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Constant;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Effective;
import com.elster.jupiter.util.conditions.Exists;
import com.elster.jupiter.util.conditions.FragmentExpression;
import com.elster.jupiter.util.conditions.Membership;
import com.elster.jupiter.util.conditions.Not;
import com.elster.jupiter.util.conditions.Or;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryBuilder {

    private final List<QueryBuilderOperation> operations = new ArrayList<>();

    public static QueryBuilder parse(Condition condition) {
        QueryBuilder queryBuilder = new QueryBuilder();
        condition.visit(queryBuilder.new QueryBuilderVisitor());
        return queryBuilder;
    }

    public static QueryBuilder using(List<? extends QueryBuilderOperation> operations) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.operations.addAll(operations);
        return queryBuilder;
    }

    public Condition toCondition() {
        ConditionBuilderVisitor conditionBuilderVisitor = new ConditionBuilderVisitor();
        for (QueryBuilderOperation operation : operations) {
            operation.visit(conditionBuilderVisitor);
        }
        return conditionBuilderVisitor.pop();
    }

    private class QueryBuilderVisitor implements Visitor {

        @Override
        public void visitOr(Or or) {
            add(OpenBracketOperation.atPosition(nextPosition()));
            int numberOfConditions = or.getConditions().size();
            for (int i = 0; i < numberOfConditions; i++) {
                or.getConditions().get(i).visit(this);
                if (i != (numberOfConditions - 1)) {
                    add(OrOperation.atPosition(nextPosition()));
                }
            }
            add(CloseBracketOperation.atPosition(nextPosition()));
        }

        private int nextPosition() {
            return operations.size() + 1;
        }

        @Override
        public void visitAnd(And and) {
            add(OpenBracketOperation.atPosition(nextPosition()));
            int numberOfConditions = and.getConditions().size();
            for (int i = 0; i < numberOfConditions; i++) {
                and.getConditions().get(i).visit(this);
                if (i != (numberOfConditions - 1)) {
                    add(AndOperation.atPosition(nextPosition()));
                }
            }
            add(CloseBracketOperation.atPosition(nextPosition()));
        }

        @Override
        public void visitComparison(Comparison comparison) {
            SimpleConditionOperation operation = new SimpleConditionOperation(comparison);
            operation.setPosition(nextPosition());
            add(operation);
        }

        @Override
        public void visitNot(Not not) {
            add(NotOperation.atPosition(nextPosition()));
            add(OpenBracketOperation.atPosition(nextPosition()));
            not.getNegated().visit(this);
            add(CloseBracketOperation.atPosition(nextPosition()));
        }

        @Override
        public void visitTrue(Constant trueCondition) {
            //adding no conditions
            //throw new UnsupportedOperationException("True constant not supported.");
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

        @Override
        public void visitEffective(Effective effective) {
        	throw new UnsupportedOperationException("Effective expressions not YET supported.");
        }
    }

    private void add(QueryBuilderOperation operation) {
        operations.add(operation);
    }

    public List<QueryBuilderOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }
}
