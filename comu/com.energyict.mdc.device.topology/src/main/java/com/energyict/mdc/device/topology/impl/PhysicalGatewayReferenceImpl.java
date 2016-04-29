package com.energyict.mdc.device.topology.impl;


import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;


/**
 * Represent's the defaultPhysicalGateWayReference - used to set validation
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF +"}")
public class PhysicalGatewayReferenceImpl extends AbstractPhysicalGatewayReferenceImpl {

    public PhysicalGatewayReferenceImpl createFor(Device origin, Device master, Interval interval) {
        super.createFor(origin, master, interval);
        return this;
    }

}