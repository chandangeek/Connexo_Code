package com.elster.jupiter.ids;

import java.util.List;

import com.elster.jupiter.util.Pair;

public interface RecordSpec {
	String getComponentName();
	long getId();
	String getName();
    List<? extends FieldSpec> getFieldSpecs();
	FieldSpec addFieldSpec(String name, FieldType type);
	Pair<? extends FieldSpec, ? extends FieldSpec> addDerivedFieldSpec(String derivedName,String rawName, FieldType fieldType, FieldDerivationRule rule);
	void persist();
}
