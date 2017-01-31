/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecordSpecImpl implements RecordSpec {
	// persistent fields
	private String componentName;
	private long id;
	private String name;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
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


	FieldSpecImpl addFieldSpec(String name, FieldType fieldType) {
		FieldSpecImpl fieldSpec = fieldSpecProvider.get().init(this,name, fieldType);
		fieldSpecs.add(fieldSpec);
		return fieldSpec;
	}

	void persist() {
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

    List<String> columnNames() {
    	int textSlotCount = 0;
    	int slotCount = 0;
    	List<String> result = new ArrayList<>(fieldSpecs.size());
    	for (FieldSpec spec : fieldSpecs) {
    		if (spec.getType().equals(FieldType.TEXT)) {
    			result.add("TEXTSLOT" + textSlotCount++);
    		} else {
    			result.add("SLOT" + slotCount++);
    		}
    	}
    	return result;
    }

	String columnName(FieldSpec fieldSpec) {
		int textSlotCount = 0;
		int slotCount = 0;
		for (FieldSpec spec : fieldSpecs) {
			String name;
			if (spec.getType().equals(FieldType.TEXT)) {
				name = "TEXTSLOT" + textSlotCount++;
			} else {
				name = "SLOT" + slotCount++;
			}
			if (spec.equals(fieldSpec)) {
				return name;
			}
		}
		throw new IllegalArgumentException("No such FieldSpec: " + fieldSpec.getName());
	}

}
