package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the {@link ConnectionTaskHeatMap} for {@link DeviceType}s,
 * providing the overview of which DeviceType has
 * the most broken connections,...
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:02)
 */
@ProviderType
public interface ConnectionTaskDeviceTypeHeatMap extends ConnectionTaskHeatMap<DeviceType> {
}