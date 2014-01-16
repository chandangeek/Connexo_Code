package com.elster.jupiter.orm.query.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.conditions.*;

public class ParentSetter implements Visitor {
	
	private final List<String> fieldNames = new ArrayList<>();
	private final List<Comparison> candidates = new ArrayList<>();
	private final List<?> results;
	
	private ParentSetter(JoinTreeNode<?> root,List<?> results) {
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
	}
	
	public void visitEffective(Effective effective) {
	}
	
	public void visitNot(Not ignored) {
	}

	public void visitTrue(Constant ignored) {
	}
	
	public void visitFalse(Constant ignored) {
	}

	@Override
	public void visitMembership(Membership membership) {
	}

	@Override
	public void visitExists(Exists empty) {
	}

	@Override
	public void visitText(Text expression) {
	}

	@Override
	public void visitFragmentExpression(FragmentExpression expression) {
	}
	
}
