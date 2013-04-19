package com.elster.jupiter.ids;

public interface FieldSpec {
	RecordSpec getRecordSpec();
	String getName();
	FieldType getType();
	FieldDerivationRule getDerivationRule();
	boolean isDerived();
}
