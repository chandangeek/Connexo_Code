package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.time.UtcInstant;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldSpecImpl implements FieldSpec {
	// persistent fields
	private String componentName;
	private long recordSpecId;
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
	private RecordSpec recordSpec;

	@SuppressWarnings("unused")
	private FieldSpecImpl()  {		
	}
	
	FieldSpecImpl(RecordSpec recordSpec , String name , FieldType fieldType) {
		this.recordSpec = recordSpec;
		this.componentName = recordSpec.getComponentName();
		this.recordSpecId = recordSpec.getId();
		this.name = name;
		this.fieldType = fieldType;
		this.derivationRule = FieldDerivationRule.NODERIVATION;
	}
	
	private FieldType getFieldSpecType() {
		return fieldType;
	}
	
	@Override
	public RecordSpec getRecordSpec() {
		if (recordSpec == null) {
			recordSpec = Bus.getOrmClient().getRecordSpecFactory().getExisting(componentName, recordSpecId);
		}
		return recordSpec;
	}
	
	void doSetRecordSpec(RecordSpec recordSpec) {
		this.recordSpec = recordSpec;
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
		getFactory().persist(this);		
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
	
	private DataMapper<FieldSpec> getFactory() {
		return Bus.getOrmClient().getFieldSpecFactory();
	}
	
}
