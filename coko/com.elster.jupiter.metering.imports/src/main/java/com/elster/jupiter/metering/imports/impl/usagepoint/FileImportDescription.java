package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FileImportField;

import java.util.List;
import java.util.Map;

public interface FileImportDescription<T extends FileImportRecord> {

    T getFileImportRecord();

    Map<String, FileImportField<?>> getFields(T record);

}
