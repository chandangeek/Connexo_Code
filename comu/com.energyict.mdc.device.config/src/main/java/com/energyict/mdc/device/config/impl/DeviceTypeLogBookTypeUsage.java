package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookType;

/**
 * Models the fact that a {@link DeviceType} uses a {@link LogBookType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeLogBookTypeUsage {
    DeviceType deviceType;
    LogBookType logBookType;
}