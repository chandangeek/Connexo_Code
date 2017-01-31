/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.fields;

public interface FieldSetter<T> {

    void setField(T value);

    default void setFieldWithHeader(String header, T value) {
        setField(value);
    }

}
