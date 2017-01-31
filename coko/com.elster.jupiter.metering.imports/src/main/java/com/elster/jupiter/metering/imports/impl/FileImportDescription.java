/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.metering.imports.impl.fields.FileImportField;

import java.util.Map;

public interface FileImportDescription<T extends FileImportRecord> {

    T getFileImportRecord();

    Map<String, FileImportField<?>> getFields(T record);

    Map<Class, FieldParser> getParsers();
}
