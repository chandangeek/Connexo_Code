package com.elster.jupiter.ids;

import com.elster.jupiter.util.Pair;

import java.util.List;

public interface RecordSpec {
	String getComponentName();
	long getId();
	String getName();
    List<? extends FieldSpec> getFieldSpecs();
	Pair<? extends FieldSpec, ? extends FieldSpec> addDerivedFieldSpec(String derivedName,String rawName, FieldType fieldType, FieldDerivationRule rule);
}
