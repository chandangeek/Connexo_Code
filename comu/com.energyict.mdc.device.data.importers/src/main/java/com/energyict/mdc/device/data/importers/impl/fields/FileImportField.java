package com.energyict.mdc.device.data.importers.impl.fields;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

import java.util.function.Consumer;

public interface FileImportField<R> {
    boolean isMandatory();

    Consumer<R> getResultConsumer();

    FieldParser<R> getParser();

    boolean isRepetitive();
}
