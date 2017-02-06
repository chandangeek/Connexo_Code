/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport;

import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;

import java.util.Map;

public interface FileImportDescription<T extends FileImportRecord> {

    T getFileImportRecord();

    Map<String, FileImportField<?>> getFields(T record);

    Map<Class, FieldParser> getParsers();
}
