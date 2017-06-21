package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.upl.properties.DeviceGroup;

import java.util.Collection;

/**
 * Structure to specify DeviceMessageQuery
 */
public interface DeviceMessageQueryFilter {
    Collection<EndDeviceGroup> getDeviceGroups();

}
