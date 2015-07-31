package com.energyict.mdc.device.data.importers.impl.fields;

@FunctionalInterface
public interface FieldSetter<T> {

    void setField(T value);

    default void setFieldWithHeader(String header, T value) {
        setField(value);
    }

}
