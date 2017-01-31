/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DomainMapper;

abstract class MultiColumnFragment extends AliasFragment {

	private final MultiColumnMapping fieldMapping;

	MultiColumnFragment(MultiColumnMapping fieldMapping , String alias) {
		super(alias);
		this.fieldMapping = fieldMapping;
	}

	final String reduce(Column column) {
		return column.getFieldName().substring(fieldMapping.getFieldName().length() + 1);
	}
	
	MultiColumnMapping getFieldMapping() {
		return fieldMapping;
	}
	
	final int bind(PreparedStatement statement, int position , Object value) throws SQLException {		
		for (ColumnImpl each : getFieldMapping().getColumns()) {
			Object subValue = DomainMapper.FIELDSTRICT.get(value, reduce(each));
			statement.setObject(position++ , each.convertToDb(subValue));
		}
		return position;
	}
	

}