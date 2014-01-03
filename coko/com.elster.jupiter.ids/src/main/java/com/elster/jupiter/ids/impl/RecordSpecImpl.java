package com.elster.jupiter.ids.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

public final class RecordSpecImpl implements RecordSpec {
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
	private List<FieldSpec> fieldSpecs = new ArrayList<>();
	
	private final DataModel dataModel;
	
	@Inject
	RecordSpecImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	RecordSpecImpl init(String componentName , long id , String name) {
		this.componentName = Objects.requireNonNull(componentName);
		this.id = id;
		this.name = Objects.requireNonNull(name);
		return this;
	}
	
	public static RecordSpecImpl from(DataModel dataModel, String componentName, long id,String name ) {
		return dataModel.getInstance(RecordSpecImpl.class).init(componentName, id, name);
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
		return ImmutableList.copyOf(fieldSpecs);
	}


	@Override
	public FieldSpec addFieldSpec(String name, FieldType fieldType) {		
		FieldSpec fieldSpec = FieldSpecImpl.from(dataModel,this,name, fieldType);
		fieldSpecs.add(fieldSpec);
		return fieldSpec;
	}
	
	public void persist() {
		dataModel.persist(this);
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
        return Objects.hash(id, componentName);
    }
	
}
