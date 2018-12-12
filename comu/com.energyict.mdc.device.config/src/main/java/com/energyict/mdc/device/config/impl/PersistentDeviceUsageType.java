/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceUsageType;

/**
 * The persistent version of {@link DeviceUsageType}
 * that adds "reserved" values to make sure that the old
 * EIServer database schemas containing existing
 * device types can be read without problems.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:59)
 */
public enum PersistentDeviceUsageType {

    NONE(0, DeviceUsageType.NONE),
    METER(1, DeviceUsageType.METER),
    CONVERTOR(2, DeviceUsageType.CONVERTOR),
    CALCULATOR(3, DeviceUsageType.CALCULATOR),
    OTHER(999, DeviceUsageType.OTHER);

    private int code;
    private DeviceUsageType actualType;

    private PersistentDeviceUsageType(int code, DeviceUsageType actualType) {
        this.code = code;
        this.actualType = actualType;
    }

    public int getCode() {
        return code;
    }

    public DeviceUsageType toActualType() {
        return this.actualType;
    }

    public static PersistentDeviceUsageType fromDb (int persistentValue) {
        for (PersistentDeviceUsageType persistentType : values()) {
            if (persistentValue == persistentType.getCode()) {
                return persistentType;
            }
        }
        throw new RuntimeException("No applicable PersistentDeviceUsageType found for db value" + persistentValue);
    }

    public static PersistentDeviceUsageType fromActual (DeviceUsageType actualType) {
        for (PersistentDeviceUsageType persistentType : values()) {
            if (persistentType.toActualType().equals(actualType)) {
                return persistentType;
            }
        }
        throw new RuntimeException("No applicable PersistentDeviceUsageType found for actual type " + actualType);
    }

}
