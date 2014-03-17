package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exception.MessageSeeds;
import com.energyict.mdc.device.data.impl.constraintvalidators.CantBeOwnGateway;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:58
 */
@CantBeOwnGateway(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Constants.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY +"}")
public class CommunicationGatewayReferenceImpl implements CommunicationGatewayReference {

    private Reference<Device> origin = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> gateway = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.VALUE_IS_REQUIRED_KEY + "}")
    private Interval interval;

    @Inject
    public CommunicationGatewayReferenceImpl() {
    }

    public CommunicationGatewayReferenceImpl createFor(Interval interval, Device gateway, Device origin) {
        this.interval = interval;
        this.gateway.set(gateway);
        this.origin.set(origin);
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
    public Device getCommunicationGateway() {
        return gateway.orNull();
    }

    @Override
    public void terminate(Date closingDate) {
        if (!interval.isEffective(closingDate)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(closingDate);
    }

    @Override
    public Device getOrigin() {
        return origin.orNull();
    }
}
