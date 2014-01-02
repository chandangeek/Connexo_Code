package com.elster.jupiter.orm.query.impl;

import java.util.List;

import com.elster.jupiter.util.conditions.*;

public class JoinTreeMarker implements Visitor {
	
	private final JoinTreeNode<?> root;
	
	private JoinTreeMarker(JoinTreeNode<?> root) {
		this.root = root;
	}
	
	static JoinTreeMarker on(JoinTreeNode<?> root) {
		return new JoinTreeMarker(root);
	}
	
	void visit(Condition condition) {
		condition.visit(this);
	}
		
	private void visitAll(List<Condition> conditions , String separator) {		
		for (Condition each : conditions) {
			each.visit(this);			
		}		
	}

	public void visitAnd(And and) {
		visitAll(and.getConditions()," AND ");
	}
	
	public void visitOr(Or or) {
		visitAll(or.getConditions()," OR ");
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
		boolean markAndTest = root.hasWhereField(fieldName);
		if (!markAndTest) {
			throw new IllegalArgumentException("Invalid field name " + fieldName); 
		} 		
	}
	public void visitNot(Not not) {
		not.getNegated().visit(this);
	}

	public void visitTrue(Constant ignored) {
	}
	
	public void visitFalse(Constant ignored) {
	}

	@Override
	public void visitMembership(Membership membership) {
		for (String fieldName : membership.getFieldNames()) {
			ColumnAndAlias columnAndAlias = root.getColumnAndAliasForField(fieldName);
			if (columnAndAlias == null) {
				throw new IllegalArgumentException("Invalid field: " + fieldName);
			}
		}
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
