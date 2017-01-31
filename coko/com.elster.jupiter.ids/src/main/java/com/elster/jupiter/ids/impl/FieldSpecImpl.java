/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class FieldSpecImpl implements FieldSpec {
	
	@SuppressWarnings("unused")
	private int position;
	private String name;
	private FieldType fieldType;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	
	//associations
	private Reference<RecordSpec> recordSpec = ValueReference.absent();
	
    @Inject
	FieldSpecImpl()  {
	}
	
	FieldSpecImpl init(RecordSpec recordSpec, String name, FieldType fieldType) {
		this.recordSpec.set(recordSpec);
		this.name = name;
		this.fieldType = fieldType;
		return this;
	}
	
	private FieldType getFieldSpecType() {
		return fieldType;
	}
	
	@Override
	public RecordSpec getRecordSpec() {
		return recordSpec.get();
	}
	
	@Override
	public String getName() {	
		return name;
	}

	@Override
	public FieldType getType() {
		return fieldType;
	} 

	Object getValue(ResultSet resultSet, int i) throws SQLException {
		return getFieldSpecType().getValue(resultSet,i);
	}

	void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
		getFieldSpecType().bind(statement, offset, object);
	}
}
