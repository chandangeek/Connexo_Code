/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.Setter;
import com.elster.jupiter.util.sql.SqlFragment;

public class ConstraintEqualFragment extends ConstraintFragment implements SqlFragment , Setter {

	private final Object value;
	
	public ConstraintEqualFragment(ForeignKeyConstraintImpl constraint , Object value , String alias) {
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
		DomainMapper.FIELDSTRICT.set(target, getConstraint().getFieldName(), value);
	}
	
}
