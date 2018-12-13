/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
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
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Or;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;

import java.util.ArrayList;
import java.util.List;

class ParentSetter implements Visitor {

	private final List<String> fieldNames = new ArrayList<>();
	private final List<Comparison> candidates = new ArrayList<>();
	private final List<?> results;

	private ParentSetter(JoinTreeNode<?> root, List<?> results) {
		for (ForeignKeyConstraintImpl constraint : root.getTable().getForeignKeyConstraints()) {
			String fieldName = constraint.getFieldName();
			if (fieldName != null) {
				fieldNames.add(fieldName);
			}
		}
		this.results = results;
	}

	static ParentSetter on(JoinTreeNode<?> root,List<?> result) {
		return new ParentSetter(root,result);
	}

	void visit(Condition condition) {
		if (fieldNames.isEmpty() || results.isEmpty()) {
			return;
		}
		condition.visit(this);
		for (Comparison comparison : candidates) {
			if (condition.implies(comparison)) {
				for (Object each : results) {
					DomainMapper.FIELDSTRICT.set(each, comparison.getFieldName() , comparison.getValues()[0]);
				}
			}
		}
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
		if (comparison.getOperator().equals(Operator.EQUAL) && fieldNames.contains(comparison.getFieldName())) {
			candidates.add(comparison);
		}
	}

	public void visitContains(Contains contains) {
		// Not of interest for now
	}

	public void visitEffective(Effective effective) {
        // Not of interest for now
	}

	public void visitNot(Not ignored) {
        // Not of interest for now
	}

	public void visitTrue(Constant ignored) {
        // Not of interest for now
	}

	public void visitFalse(Constant ignored) {
        // Not of interest for now
	}

	@Override
	public void visitMembership(Membership membership) {
        // Not of interest for now
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

}