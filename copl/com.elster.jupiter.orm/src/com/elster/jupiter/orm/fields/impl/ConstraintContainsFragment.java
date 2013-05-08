package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.util.conditions.Contains;

public class ConstraintContainsFragment extends ConstraintFragment {
	
	private final Contains contains;
	
	public ConstraintContainsFragment(ForeignKeyConstraint constraint , Contains contains , String alias) {
		super(constraint,alias);
		this.contains = contains;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : contains.getCollection()) {
			position = bind(statement, position,value);
		}		
		return position;
	}
	
	public String getText() {
		int keyParts = getConstraint().getColumns().size();
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (int i = 0 ; i < keyParts; i++) {
			builder.append(separator);
			builder.append(getConstraint().getColumns().get(i).getName(getAlias()));
			separator = ", ";
		}
		builder.append(") ");		
		builder.append(contains.getOperator().getSymbol());
		builder.append(" (");
		String outerSeparator = "";
		for (int i = 0 ; i < contains.getCollection().size() ; i++) {
			builder.append(outerSeparator);
			if (keyParts == 1) {
				builder.append("?");
			} else {
				String innerSeparator = "";
				builder.append("(");
				for (int j = 0 ; j < keyParts ; j++) {
					builder.append(innerSeparator);
					builder.append("?");
					innerSeparator = ",";
				}
				builder.append(")");
			}
			outerSeparator = ",";
		}
		builder.append(")");
		return builder.toString();
	}
	
}


