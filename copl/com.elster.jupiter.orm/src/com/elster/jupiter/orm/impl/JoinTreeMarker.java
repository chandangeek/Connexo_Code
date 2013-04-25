package com.elster.jupiter.orm.impl;

import java.util.List;

import com.elster.jupiter.conditions.*;

public class JoinTreeMarker implements Visitor {
	
	final private JoinTreeNode<?> root;
	final private int pass;
	
	JoinTreeMarker(JoinTreeNode<?> root, int pass) {
		this.root = root;
		this.pass = pass;
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
		// do not mark for inner join , if operator is null
		// to allow for anti joins
		if (!(pass == 2 && comparison.getOperator() == Operator.ISNULL)) {			
			markAndTest(comparison.getFieldName());
		}
	}

	public void visitContains(Contains contains) {
		markAndTest(contains.getFieldName());		 		
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
	
}
