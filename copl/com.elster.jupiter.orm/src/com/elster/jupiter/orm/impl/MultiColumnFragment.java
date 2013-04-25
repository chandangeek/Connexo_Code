package com.elster.jupiter.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.Column;

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
		for (Column each : getFieldMapping().getColumns()) {
			Object subValue = DomainMapper.FIELD.get(value, reduce(each));
			statement.setObject(position++ , ((ColumnImpl) each).convertToDb(subValue));
		}
		return position;
	}
	

}