package com.energyict.mdc.device.data.importers.impl.fields;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

public interface FileImportField<R> {

    boolean isMandatory();

    FieldSetter<R> getSetter();

    FieldParser<R> getParser();

    boolean isRepetitive();
}
