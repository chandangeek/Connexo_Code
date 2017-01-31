/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.impl.ColumnImpl;

abstract class ColumnFragment extends AliasFragment {

	private final ColumnImpl column;

	public ColumnFragment(ColumnImpl column , String alias) {
		super(alias);
		this.column = column;
	}
	
	ColumnImpl getColumn() {
		return column;
	}
	
	int bind(PreparedStatement statement, int position , Object value) throws SQLException {		
		statement.setObject(position++ , column.convertToDb(value));
		return position;
	}

}