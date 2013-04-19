package com.elster.jupiter.orm.impl;

import java.util.List;

import com.elster.jupiter.conditions.*;
import com.elster.jupiter.sql.util.SqlBuilder;

public class WhereClauseBuilder implements Visitor {
	
	final private JoinTreeNode<?> root;
	final private SqlBuilder builder;
	
	WhereClauseBuilder(JoinTreeNode<?> root, SqlBuilder builder) {
		this.root = root;
		this.builder = builder;
	}
	
	public void visit(Condition condition) {
		condition.visit(this);
	}
		
	public void visitAll(List<Condition> conditions , String separator) {		
		boolean firstTime = true; 
		builder.openBracket();
		for (Condition each : conditions) {
			if (firstTime) {
				firstTime = false;
			} else {
				builder.append(separator);
			}
			each.visit(this);			
		}		
		builder.closeBracket();
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
		ComparisonFragment holder = new ComparisonFragment(columnAndAlias, comparison);
		builder.add(holder);
	}

	public void visitNot(Not not) {
		builder.append(" NOT ");
		builder.openBracket();
		not.getNegated().visit(this);
		builder.closeBracket();
	}

	public void visitTrue(Constant ignored) {
		builder.append(" 1 = 1");
	}
	
	public void visitFalse(Constant ignored) {
		builder.append(" 1 = 0 ");
	}

	@Override
	public void visitMembership(Membership membership) {
		builder.openBracket();
		String separator = "" ;
		for (String fieldName : membership.getFieldNames()) {
			ColumnAndAlias columnAndAlias = root.getColumnAndAliasForField(fieldName);
			if (columnAndAlias == null) {
				throw new IllegalArgumentException("Invalid field: " + fieldName);
			}
			builder.append(separator);
			builder.append(columnAndAlias.toString());
			separator = ", ";
		}
		builder.closeBracketSpace();
		builder.append(membership.getOperator().getSymbol());
		builder.spaceOpenBracket();
		builder.add(((SubQueryExecutor) membership.getClub()).getSqlBuilder());
		builder.closeBracket();
	}

	@Override
	public void visitEmpty(Exists empty) {
		builder.append(" EXISTS ");
		builder.openBracket();
		builder.add(((SubQueryExecutor) empty.getClub()).getSqlBuilder());
		builder.closeBracket();		
	}
	
}
