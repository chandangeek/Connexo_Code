/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

public interface Visitor {

    void visitOr(Or or);

    void visitAnd(And and);

    void visitComparison(Comparison comparison);

    void visitNot(Not not);

    void visitTrue(Constant trueCondition);

    void visitFalse(Constant falseCondition);

    void visitContains(Contains contains);

    void visitMembership(Membership member);

    void visitExists(Exists empty);

    void visitText(Text expression);

    void visitFragmentExpression(FragmentExpression expression);

	void visitEffective(Effective effective);
}
