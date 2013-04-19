package com.elster.jupiter.orm.impl;

import java.util.List;

import com.elster.jupiter.conditions.*;

public class JoinTreeMarker implements Visitor {
	
	final private JoinTreeNode<?> root;
	
	JoinTreeMarker(JoinTreeNode<?> root) {
		this.root = root;
	}
	
	public void visit(Condition condition) {
		condition.visit(this);
	}
		
	public void visitAll(List<Condition> conditions , String separator) {		
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
		ColumnAndAlias columnAndAlias = root.getColumnAndAliasForField(comparison.getFieldName());
		if (columnAndAlias == null) {
			throw new IllegalArgumentException("Invalid field name " + comparison.getFieldName()); 
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
	public void visitEmpty(Exists empty) {
	}
	
}
