package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlFragment;

class ConstraintEqualFragment extends ConstraintFragment implements SqlFragment , Setter {

	private final Object value;
	
	public ConstraintEqualFragment(TableConstraint constraint , Object value , String alias) {
		super(constraint,alias);
		this.value = value;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		return bind(statement,position,value);		
	}
	
	@Override
	public String getText() {
		int repeat = getConstraint().getColumns().size();		
		StringBuilder builder = new StringBuilder(repeat == 1 ? " " : " (");
		String separator = "";
		for (int i = 0 ; i < repeat ; i++) {
			builder.append(separator);
			builder.append(getConstraint().getColumns().get(i).getName(getAlias()));
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
		DomainMapper.FIELD.set(target, getConstraint().getFieldName(), value);
	}
	
}
