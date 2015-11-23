package com.elster.jupiter.ids;

import java.util.List;

public interface RecordSpec {
	String getComponentName();
	long getId();
	String getName();
    List<? extends FieldSpec> getFieldSpecs();
}
