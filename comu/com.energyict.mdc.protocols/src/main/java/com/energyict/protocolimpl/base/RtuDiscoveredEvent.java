/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.BaseDevice;

public class RtuDiscoveredEvent {

    private final BaseDevice device;

    /**
     * Creates a new instance of a <code>RtuDiscoveredEvent</code>.
     *
     * @param device the <code>Device</code> being discovered.
     */
    public RtuDiscoveredEvent(BaseDevice device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "RtuDiscoveredEvent from device [" + device + "]";
    }

}