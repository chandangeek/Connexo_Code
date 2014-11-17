package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.util.time.Interval;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:58
 */
public class CommunicationGatewayReferenceImpl extends GatewayReferenceImpl implements CommunicationGatewayReference {

    public CommunicationGatewayReferenceImpl createFor(Interval interval, Device gateway, Device origin) {
        this.init(interval, gateway, origin);
        return this;
    }

}