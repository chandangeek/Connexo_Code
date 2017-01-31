/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;

public class MultiColumnEqualFragment extends MultiColumnFragment {

	private final Object value;
	
	public MultiColumnEqualFragment(MultiColumnMapping fieldMapping , Object value , String alias) {
		super(fieldMapping , alias);
		this.value = value;		
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {		
		return bind(statement , position , value);
	}
	
	@Override
	public String getText() {
		StringBuilder builder = new StringBuilder(" ");
		builder.append("(");
		String separator = "";
		for (Column each : getFieldMapping().getColumns())  {
			builder.append(separator);
			builder.append(each.getName(getAlias()));			
			builder.append(" = ? " );
			separator = " AND ";
		}
		builder.append(")");
		return builder.toString();
	}
	

}
