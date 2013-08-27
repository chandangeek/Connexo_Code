package com.elster.jupiter.ids;

import java.util.List;

public interface RecordSpec {
	String getComponentName();
	long getId();
	String getName();
    List<FieldSpec> getFieldSpecs();
	FieldSpec addFieldSpec(String name, FieldType type);
	void persist();
}
