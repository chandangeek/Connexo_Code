package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.conditions.Comparison;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ConstraintComparisonFragment implements SqlFragment {
	private final TableConstraint constraint;
	private final Comparison comparison;
	private final String alias;
	
	public ConstraintComparisonFragment(TableConstraint constraint , Comparison comparison , String alias) {
		this.constraint = constraint;
		this.comparison = comparison;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		FieldMapper fieldMapper = new FieldMapper();
		for (Object value : comparison.getValues()) {
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
		int keyParts = constraint.getColumns().size();
		StringBuilder builder = new StringBuilder("(");
		String separator = "";
		for (int i = 0 ; i < keyParts; i++) {
			builder.append(separator);
			builder.append(constraint.getColumns().get(i).getName(alias));
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


