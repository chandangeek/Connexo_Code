package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PhysicalGatewayReference;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.constraintvalidators.CantBeOwnGateway;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
@CantBeOwnGateway(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY +"}")
public class PhysicalGatewayReferenceImpl implements PhysicalGatewayReference {

    public enum Field implements ImplField {
        CREATION_TIME("interval.start"),
        GATEWAY("gateway"),
        ;

        private final String javaFieldName;

        private Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Device> origin = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> gateway = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Interval interval;

    @Inject
    public PhysicalGatewayReferenceImpl() {
    }

    public PhysicalGatewayReferenceImpl createFor(Interval interval, Device master, Device origin) {
        this.interval = interval;
        this.gateway.set(master);
        this.origin.set(origin);
        return this;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean existsFor(Instant existenceDate) {
        return isEffectiveAt(existenceDate);
    }

    @Override
    public Device getPhysicalGateway() {
        return gateway.orNull();
    }

    @Override
    public void terminate(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(closingDate);
    }

    @Override
    public Device getOrigin(){
        return this.origin.orNull();
    }

}