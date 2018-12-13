/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.upl.properties.DeviceGroup;

/**
 * Holds an {@link EndDeviceGroup} and publishes it as a {@link DeviceGroup}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-03 (13:17)
 */
public class UPLEndDeviceGroupHolder implements DeviceGroup {
    private final EndDeviceGroup actual;

    public static UPLEndDeviceGroupHolder from(EndDeviceGroup deviceGroup) {
        return new UPLEndDeviceGroupHolder(deviceGroup);
    }

    private UPLEndDeviceGroupHolder(EndDeviceGroup actual) {
        this.actual = actual;
    }

    public EndDeviceGroup getEndDeviceGroup() {
        return actual;
    }
}