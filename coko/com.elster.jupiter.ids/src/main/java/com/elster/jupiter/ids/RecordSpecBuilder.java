package com.elster.jupiter.ids;

public interface RecordSpecBuilder {

    RecordSpecBuilder addFieldSpec(String name, FieldType type);

    RecordSpecBuilder addDerivedFieldSpec(String derivedName, String rawName, FieldType fieldType, FieldDerivationRule rule);

    RecordSpec create();
}
