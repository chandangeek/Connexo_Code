package com.elster.jupiter.ids.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;

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
	private final Provider<FieldSpecImpl> fieldSpecProvider;
	
	@Inject
	RecordSpecImpl(DataModel dataModel,Provider<FieldSpecImpl> fieldSpecProvider) {
		this.dataModel = dataModel;
		this.fieldSpecProvider = fieldSpecProvider;
	}
	
	RecordSpecImpl init(String componentName , long id , String name) {
		this.componentName = Objects.requireNonNull(componentName);
		this.id = id;
		this.name = Objects.requireNonNull(name);
		return this;
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
	public FieldSpecImpl addFieldSpec(String name, FieldType fieldType) {		
		FieldSpecImpl fieldSpec = fieldSpecProvider.get().init(this,name, fieldType);
		fieldSpecs.add(fieldSpec);
		return fieldSpec;
	}
	
	@Override
	public Pair<FieldSpecImpl,FieldSpecImpl> addDerivedFieldSpec(String derivedName, String rawName, FieldType fieldType, FieldDerivationRule rule) {
		FieldSpecImpl derived = fieldSpecProvider.get().init(this,derivedName,fieldType,rule);
		fieldSpecs.add(derived);
		return Pair.of(derived, addFieldSpec(rawName,fieldType));
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
    
    int derivedFieldCount() {
    	int result = 0;
    	for (FieldSpec field : getFieldSpecs()) {
    		if (field.isDerived()) {
    			result++;
    		}
    	}
    	return result;
    }
	
}
