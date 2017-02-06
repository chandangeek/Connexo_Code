/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.csvimport.fields;

public interface FieldSetter<T> {

    void setField(T value);

    default void setFieldWithHeader(String header, T value) {
        setField(value);
    }

}
