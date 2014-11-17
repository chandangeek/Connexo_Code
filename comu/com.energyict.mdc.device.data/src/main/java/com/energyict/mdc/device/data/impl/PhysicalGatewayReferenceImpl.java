package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.util.time.Interval;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
public class PhysicalGatewayReferenceImpl extends GatewayReferenceImpl implements PhysicalGatewayReference {

    public PhysicalGatewayReferenceImpl createFor(Interval interval, Device master, Device origin) {
        this.init(interval, master, origin);
        return this;
    }

}