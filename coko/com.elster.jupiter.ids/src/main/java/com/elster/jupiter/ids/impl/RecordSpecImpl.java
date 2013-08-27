package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

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
		return ImmutableList.copyOf(doGetFieldSpecs());
	}
	
	private List<FieldSpec> doGetFieldSpecs() {
		if (fieldSpecs == null) {
			fieldSpecs = Bus.getOrmClient().getFieldSpecFactory().find("recordSpec", this);
			for (FieldSpec each : fieldSpecs) {
				((FieldSpecImpl) each).doSetRecordSpec(this);
			}
		}
		return fieldSpecs;
	}

	@Override
	public FieldSpec addFieldSpec(String name, FieldType fieldType) {		
		FieldSpec fieldSpec = new FieldSpecImpl(this,name, fieldType);
		doGetFieldSpecs().add(fieldSpec);
		((FieldSpecImpl) fieldSpec).doSetPosition(doGetFieldSpecs().size());
		return fieldSpec;
	}
	
	public void persist() {
		getFactory().persist(this);
		for (FieldSpec fieldSpec : doGetFieldSpecs()) {
			((FieldSpecImpl) fieldSpec).persist();
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecordSpecImpl that = (RecordSpecImpl) o;

        return id == that.id && componentName.equals(that.componentName);

    }

    @Override
    public int hashCode() {
        int result = componentName.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
	private DataMapper<RecordSpec> getFactory() {
		return Bus.getOrmClient().getRecordSpecFactory();		
	}
}
