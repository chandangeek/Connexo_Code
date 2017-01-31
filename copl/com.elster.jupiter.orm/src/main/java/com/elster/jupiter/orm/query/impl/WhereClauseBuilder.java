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
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.List;

public class WhereClauseBuilder implements Visitor {
	
	private final JoinTreeNode<?> root;
	private final SqlBuilder builder;
	private final Instant effectiveDate;
	
	private WhereClauseBuilder(JoinTreeNode<?> root, SqlBuilder builder, Instant effectiveDate) {
		this.root = root;
		this.builder = builder;
		this.effectiveDate = effectiveDate;
	}
	
	static WhereClauseBuilder from(JoinTreeNode<?> root, SqlBuilder builder, Instant effectiveDate) {
		return new WhereClauseBuilder(root,builder,effectiveDate);
	}
	
	void visit(Condition condition) {
		condition.visit(this);
	}
		
	private void visitAll(List<Condition> conditions , String separator) {		
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
		SqlFragment fragment = root.getFragment(comparison, comparison.getFieldName());
		if (fragment == null) {
			throw new IllegalArgumentException("Invalid field name " + comparison.getFieldName());
		}
		builder.add(fragment);
	}
	
	public void visitContains(Contains contains) {
		SqlFragment fragment = root.getFragment(contains, contains.getFieldName());
		if (fragment == null) {
			throw new IllegalArgumentException("Invalid field name " + contains.getFieldName());
		}
		builder.add(fragment);
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
            List<ColumnAndAlias> columnAndAliases = root.getColumnAndAliases(fieldName);
            if (columnAndAliases.isEmpty() || columnAndAliases.size() > 1) {
                throw new IllegalArgumentException("Invalid field: " + fieldName);
            }
            ColumnAndAlias columnAndAlias = columnAndAliases.get(0);
            builder.append(separator);
            builder.append(columnAndAlias.toString());
			separator = ", ";
		}
		builder.closeBracketSpace();
		builder.append(membership.getOperator().getSymbol());
		builder.spaceOpenBracket();
		builder.add(membership.getSubquery().toFragment());
		builder.closeBracket();
	}

	@Override
	public void visitExists(Exists empty) {
		builder.append(" EXISTS ");
		builder.openBracket();
		builder.add(empty.getSubquery().toFragment());
		builder.closeBracket();		
	}

	@Override
	public void visitText(Text expression) {
		builder.append(expression.getText());
	}

	@Override
	public void visitFragmentExpression(FragmentExpression expression) {
		builder.add(expression.getFragment());
	}

	@Override
	public void visitEffective(Effective effective) {
		Where.where(effective.getFieldName()).isEffective(effectiveDate).visit(this);
	}
	
}
