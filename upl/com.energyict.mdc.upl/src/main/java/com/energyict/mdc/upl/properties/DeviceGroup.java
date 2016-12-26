package com.energyict.mdc.upl.properties;

import com.energyict.mdc.upl.meterdata.Device;

import java.util.Set;

/**
 * Models a group of devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-30 (14:45)
 */
public interface DeviceGroup {
    Set<Device> members();
}