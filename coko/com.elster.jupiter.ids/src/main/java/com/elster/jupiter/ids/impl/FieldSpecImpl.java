package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldSpecImpl implements FieldSpec {
	
	@SuppressWarnings("unused")
	private int position;
	private String name;
	private FieldType fieldType;
	private FieldDerivationRule derivationRule;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	
	//associations
	private Reference<RecordSpec> recordSpec = ValueReference.absent();
	
    @Inject
	FieldSpecImpl()  {
	}
	
	FieldSpecImpl init(RecordSpec recordSpec , String name , FieldType fieldType, FieldDerivationRule derivationRule) {
		this.recordSpec.set(recordSpec);
		this.name = name;
		this.fieldType = fieldType;
		this.derivationRule = derivationRule;
		return this;
	}
	
	FieldSpecImpl init(RecordSpec recordSpec, String name, FieldType fieldType) {
		return init (recordSpec, name, fieldType, FieldDerivationRule.NODERIVATION);
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

	@Override
	public FieldDerivationRule getDerivationRule() {
		return derivationRule;
	}

	@Override
	public boolean isDerived() {
		return getDerivationRule() != FieldDerivationRule.NODERIVATION;
	}
	
	Object getValue(ResultSet resultSet, int i) throws SQLException {
		return getFieldSpecType().getValue(resultSet,i);
	}

	void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
		getFieldSpecType().bind(statement, offset, object);
	}
}
