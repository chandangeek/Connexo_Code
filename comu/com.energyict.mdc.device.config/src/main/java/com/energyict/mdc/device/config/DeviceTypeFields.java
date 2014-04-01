package com.energyict.mdc.device.config;

public enum DeviceTypeFields {
    DEVICE_PROTOCOL_PLUGGABLE_CLASS("deviceProtocolPluggableClassId");
    private String javaFieldName;

    DeviceTypeFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String fieldName() {
        return javaFieldName;
    }
}