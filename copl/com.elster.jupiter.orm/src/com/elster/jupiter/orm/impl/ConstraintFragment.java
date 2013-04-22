package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ConstraintFragment implements SqlFragment , Setter {

	private final TableConstraint constraint;
	private final Object value;
	private final String alias;
	
	public ConstraintFragment(TableConstraint constraint , Object value , String alias) {
		this.value = value;
		this.constraint =  constraint;
		this.alias = alias;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		FieldMapper mapper = new FieldMapper();
		for (int i = 0 ; i < constraint.getColumns().size(); i++) {
			Object columnValue = value == null ? null : mapper.get(value, constraint.getReferencedTable().getPrimaryKeyColumns()[i].getFieldName());
			statement.setObject(position++ , ((ColumnImpl) constraint.getColumns().get(i)).convertToDb(columnValue));
		}
		return position;
	}
	
	@Override
	public String getText() {
		int repeat = constraint.getColumns().size();		
		StringBuilder builder = new StringBuilder(repeat == 1 ? " " : " (");
		String separator = "";
		for (int i = 0 ; i < repeat ; i++) {
			builder.append(separator);
			builder.append(constraint.getColumns().get(i).getName(alias));
			builder.append(" = ? " );
			separator = " AND ";
		}
		if (repeat > 1) {
			builder.append(") ");
		}
		return builder.toString();
	}
	
	@Override
	public void set(Object target) {
		FieldMapper mapper = new FieldMapper();
		mapper.set(target, constraint.getFieldName(), value);
	}
	
}
