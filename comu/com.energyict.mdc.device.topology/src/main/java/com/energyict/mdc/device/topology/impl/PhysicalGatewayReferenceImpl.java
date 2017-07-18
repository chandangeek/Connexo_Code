/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;


import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;


@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF +"}")
public class PhysicalGatewayReferenceImpl extends AbstractPhysicalGatewayReferenceImpl {

    public PhysicalGatewayReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

}