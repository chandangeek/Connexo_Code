package com.elster.jupiter.metering.imports.impl.usagepoint.fields;


import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.FieldParser;

public interface FileImportField<R> {

    boolean isMandatory();

    FieldSetter<R> getSetter();

    FieldParser<R> getParser();

    boolean isRepetitive();

    String getFieldName();
}
