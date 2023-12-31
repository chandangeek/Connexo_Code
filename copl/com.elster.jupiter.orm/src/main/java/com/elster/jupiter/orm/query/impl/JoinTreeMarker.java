/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

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
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;

import java.util.Arrays;
import java.util.List;

public class JoinTreeMarker implements Visitor {

    private final JoinTreeNode<?> root;

    private JoinTreeMarker(JoinTreeNode<?> root) {
        this.root = root;
    }

    static JoinTreeMarker on(JoinTreeNode<?> root) {
        return new JoinTreeMarker(root);
    }

    JoinTreeMarker visit(Condition condition) {
        condition.visit(this);
        return this;
    }

    private void visitAll(List<Condition> conditions) {
        for (Condition each : conditions) {
            each.visit(this);
        }
    }

    public void visitAnd(And and) {
        visitAll(and.getConditions());
    }

    public void visitOr(Or or) {
        visitAll(or.getConditions());
    }

    public void visitComparison(Comparison comparison) {
        markAndTest(comparison.getFieldName());
    }

    public void visitContains(Contains contains) {
        markAndTest(contains.getFieldName());
    }

    public void visitEffective(Effective effective) {
        markAndTest(effective.getFieldName());
    }

    private void markAndTest(String fieldName) {
        if (!root.hasWhereField(fieldName)) {
            throw new IllegalArgumentException("Invalid field name " + fieldName);
        }
    }

    public void visitNot(Not not) {
        not.getNegated().visit(this);
    }

    public void visitTrue(Constant ignored) {
        // Not of interest for now
    }

    public void visitFalse(Constant ignored) {
        // Not of interest for now
    }

    @Override
    public void visitMembership(Membership membership) {
        for (String fieldName : membership.getFieldNames()) {
            markAndTest(fieldName);
        }
    }

    @Override
    public void visitExists(Exists empty) {
        // Not of interest for now
    }

    @Override
    public void visitText(Text expression) {
        // Not of interest for now
    }

    @Override
    public void visitFragmentExpression(FragmentExpression expression) {
        // Not of interest for now
    }

    JoinTreeMarker visit(Order... orders) {
        if (orders != null) {
            Arrays.stream(orders)
                    .map(Order::getName)
                    .forEach(root::hasWhereField); // root::hasWhereField is used instead of this::markAndTest
            // because we have a lot of order clauses constructed with column names or even formulae instead of field names.
            // they won't pass the check of this::markAndTest
        }
        return this;
    }
}
