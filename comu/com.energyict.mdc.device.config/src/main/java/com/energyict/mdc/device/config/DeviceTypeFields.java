package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ImplField;

public enum DeviceTypeFields implements ImplField {
    DEVICE_PROTOCOL_PLUGGABLE_CLASS("deviceProtocolPluggableClassId"),
    NAME("name");

    private String javaFieldName;

    DeviceTypeFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String fieldName() {
        return javaFieldName;
    }
}