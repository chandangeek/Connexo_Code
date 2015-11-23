package com.elster.jupiter.ids;

public interface RecordSpecBuilder {

    RecordSpecBuilder addFieldSpec(String name, FieldType type);

    RecordSpec create();
}
