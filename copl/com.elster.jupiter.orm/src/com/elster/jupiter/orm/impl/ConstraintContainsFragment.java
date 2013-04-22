package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Contains;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ConstraintContainsFragment implements SqlFragment {
	private final TableConstraint constraint;
	private final Contains contains;
	private final String alias;
	
	public ConstraintContainsFragment(TableConstraint constraint , Contains contains , String alias) {
		this.constraint = constraint;
		this.contains = contains;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		FieldMapper fieldMapper = new FieldMapper();
		for (Object value : contains.getCollection()) {
			position = set(statement,position,value,fieldMapper);
		}		
		return position;
	}
	
	private int set(PreparedStatement statement , int position , Object value , FieldMapper fieldMapper)  throws SQLException {
		for (Column column : constraint.getReferencedTable().getPrimaryKeyColumns()) {
			statement.setObject(position++ , ((ColumnImpl) column).convertToDb(fieldMapper.get(value, column.getFieldName())));
		}
		return position;
	}
	
	public String getText() {
		int keyParts = constraint.getColumns().size();
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (int i = 0 ; i < keyParts; i++) {
			builder.append(separator);
			builder.append(constraint.getColumns().get(i).getName(alias));
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


