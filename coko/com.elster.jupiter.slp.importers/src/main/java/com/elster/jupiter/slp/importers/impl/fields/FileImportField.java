/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.fields;


import com.elster.jupiter.slp.importers.impl.FieldParser;

public interface FileImportField<R> {

    boolean isMandatory();

    FieldSetter<R> getSetter();

    FieldParser<R> getParser();

    boolean isRepetitive();

    String getFieldName();
}
