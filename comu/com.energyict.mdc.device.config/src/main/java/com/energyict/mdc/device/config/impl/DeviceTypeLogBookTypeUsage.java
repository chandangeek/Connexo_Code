package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;

/**
 * Models the fact that a {@link DeviceType} uses a {@link LogBookType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeLogBookTypeUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<LogBookType> logBookType = ValueReference.absent();

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

    public boolean sameLogBookType (LogBookType logBookType) {
        return this.getLogBookType().getId() == logBookType.getId();
    }

}