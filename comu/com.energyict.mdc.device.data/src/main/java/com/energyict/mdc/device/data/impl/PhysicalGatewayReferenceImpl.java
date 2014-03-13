package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
public class PhysicalGatewayReferenceImpl implements PhysicalGatewayReference {

    private Reference<Device> gateway = ValueReference.absent();
    private Interval interval;

    @Inject
    public PhysicalGatewayReferenceImpl() {
    }

    public PhysicalGatewayReferenceImpl createFor(Interval interval, Device gateway) {
        this.interval = interval;
        this.gateway.set(gateway);
        return this;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean existsFor(Date existenceDate) {
        return interval.isEffective(existenceDate);
    }

    @Override
    public Device getPhysicalGateway() {
        return gateway.orNull();
    }

    @Override
    public void terminate(Date closingDate) {
        if (!interval.isEffective(closingDate)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(closingDate);
    }
}
