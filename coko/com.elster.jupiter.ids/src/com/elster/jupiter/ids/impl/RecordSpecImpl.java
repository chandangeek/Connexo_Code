package com.elster.jupiter.ids.impl;

import java.util.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.time.UtcInstant;

public class RecordSpecImpl implements RecordSpec {
	// persistent fields
	private String componentName;
	private long id;
	private String name;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private List<FieldSpec> fieldSpecs;
	
	@SuppressWarnings("unused")
	private RecordSpecImpl() {		
	}
	
	public RecordSpecImpl(String componentName , long id , String name) {
		this.componentName = componentName;
		this.id = id;
		this.name = name;
		fieldSpecs = new ArrayList<>();
	}
	
	@Override
	public String getComponentName() {		
		return componentName;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<FieldSpec> getFieldSpecs() {		
		return getFieldSpecs(true);
	}
	
	private List<FieldSpec> getFieldSpecs(boolean protect) {
		if (fieldSpecs == null) {
			fieldSpecs = Bus.getOrmClient().getFieldSpecFactory().find("recordSpec", this);
			for (FieldSpec each : fieldSpecs) {
				((FieldSpecImpl) each).doSetRecordSpec(this);
			}
		}
		return protect ? Collections.unmodifiableList(fieldSpecs) : fieldSpecs;
	}

	@Override
	public FieldSpec addFieldSpec(String name, FieldType fieldType) {		
		FieldSpec fieldSpec = new FieldSpecImpl(this,name, fieldType);
		getFieldSpecs(false).add(fieldSpec);
		((FieldSpecImpl) fieldSpec).doSetPosition(getFieldSpecs(false).size());
		return fieldSpec;
	}
	
	public void persist() {
		getFactory().persist(this);
		for (FieldSpec fieldSpec : getFieldSpecs(false)) {
			((FieldSpecImpl) fieldSpec).persist();
		}
	}

	@Override
	public boolean equals(Object other) {
		try {
			RecordSpecImpl o = (RecordSpecImpl) other;
			return this.componentName.equals(o.componentName) && this.id == o.id;
		} catch (ClassCastException ex) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.componentName.hashCode() ^ new Long(this.id).hashCode();
	}
		
	private DataMapper<RecordSpec> getFactory() {
		return Bus.getOrmClient().getRecordSpecFactory();		
	}
}
