/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * An event indicating the device, uniquely identified by its {@link DeviceIdentifier},
 * has no {@link com.energyict.mdc.protocol.api.device.BaseLogBook LogBooks} configured.
 *
 * @author sva
 * @since 14/12/12 - 9:18
 */

public class NoLogBooksForDeviceEvent {

    /**
     * The unique deviceIdentifier identifying the device which has no {@link com.energyict.mdc.protocol.api.device.BaseLogBook LogBooks} configured.
     */
    private String deviceIdentifier;

    public NoLogBooksForDeviceEvent(DeviceIdentifier deviceIdentifier) {
        super();
        this.deviceIdentifier = deviceIdentifier.getIdentifier();
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

}