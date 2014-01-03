package com.elster.jupiter.ids.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;

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
	
	private final DataModel dataModel;

    @Inject
	private FieldSpecImpl(DataModel dataModel)  {
    	this.dataModel = dataModel;
	}
	
	FieldSpecImpl init(RecordSpec recordSpec , String name , FieldType fieldType) {
		this.recordSpec.set(recordSpec);
		this.name = name;
		this.fieldType = fieldType;
		this.derivationRule = FieldDerivationRule.NODERIVATION;
		return this;
	}
	
	static FieldSpecImpl from(DataModel dataModel, RecordSpec recordSpec , String name , FieldType fieldType) {
		return dataModel.getInstance(FieldSpecImpl.class).init(recordSpec,name,fieldType);
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
	
	void persist() {
		dataModel.persist(this);		
	}

	void doSetPosition(int position) {
		this.position = position;		
	}

	Object getValue(ResultSet resultSet, int i) throws SQLException {
		return getFieldSpecType().getValue(resultSet,i);
	}

	void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
		getFieldSpecType().bind(statement, offset, object);
	}
}
