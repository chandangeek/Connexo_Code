package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.device.config.DeviceType;

/**
 * Provides an implementation for the {@link DeviceTypeBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (14:03)
 */
public class DeviceTypeBreakdownImpl extends TaskStatusBreakdownCountersImpl<DeviceType> implements DeviceTypeBreakdown {
    public DeviceTypeBreakdownImpl() {
        super();
    }
}