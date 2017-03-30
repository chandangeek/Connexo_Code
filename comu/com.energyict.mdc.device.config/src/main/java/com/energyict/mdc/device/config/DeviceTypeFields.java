/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ImplField;

/**
 * The fieldNames of the DeviceType are managed by an internal enum in DeviceTypeImpl
 */
@Deprecated
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
