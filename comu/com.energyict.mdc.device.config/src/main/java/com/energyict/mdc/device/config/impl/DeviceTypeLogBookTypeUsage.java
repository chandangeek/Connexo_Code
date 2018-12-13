/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;

import java.time.Instant;

/**
 * Models the fact that a {@link DeviceType} uses a {@link LogBookType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeLogBookTypeUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<LogBookType> logBookType = ValueReference.absent();
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    // For orm service only
    DeviceTypeLogBookTypeUsage() {
        super();
    }

    DeviceTypeLogBookTypeUsage(DeviceType deviceType, LogBookType logBookType) {
        this();
        this.deviceType.set(deviceType);
        this.logBookType.set(logBookType);
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public LogBookType getLogBookType() {
        return logBookType.get();
    }

    boolean sameLogBookType(LogBookType logBookType) {
        return this.getLogBookType().getId() == logBookType.getId();
    }

}