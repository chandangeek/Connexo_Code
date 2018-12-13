/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport.fields;

import com.elster.jupiter.fileimport.csvimport.FieldParser;

public interface FileImportField<R> {

    boolean isMandatory();

    FieldSetter<R> getSetter();

    FieldParser<R> getParser();

    boolean isRepetitive();

    String getFieldName();
}
