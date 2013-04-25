package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.orm.TableConstraint;

class ConstraintComparisonFragment extends ConstraintFragment {
	
	private final Comparison comparison;
	
	ConstraintComparisonFragment(TableConstraint constraint , Comparison comparison , String alias) {
		super(constraint,alias);
		this.comparison = comparison;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : comparison.getValues()) {
			position = bind(statement , position , value);
		}		
		return position;
	}
	
	public String getText() {
		switch (comparison.getOperator()) {
			case EQUAL:
			case ISNOTNULL:
				return getText(" AND ");
			case ISNULL:
			case NOTEQUAL:
				return getText(" OR ");			
			default:
				throw new IllegalArgumentException("Can not generate SQL on object references for " + comparison.getOperator());
		}
	}
	
	public String getText(String keySeparator) {
		int keyParts = getConstraint().getColumns().size();
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (int i = 0 ; i < keyParts; i++) {
			builder.append(separator);
			builder.append(getConstraint().getColumns().get(i).getName(getAlias()));
			builder.append(" ");
			builder.append(comparison.getOperator().getSymbol());
			builder.append("  ");
			switch (comparison.getValues().length) {
				case 0:				
					break;
				case 1:
					builder.append("? ");
					break;					
				default: {
					throw new IllegalArgumentException(" Too many arguments ");
				}
			}
			separator = keySeparator;
		}
		builder.append(")");
		return builder.toString();
	}


}


