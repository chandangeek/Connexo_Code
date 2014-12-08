package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:57
 */
@PhysicalGatewayNotSameAsOrigin(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF +"}")
public class PhysicalGatewayReferenceImpl implements PhysicalGatewayReference {

    public enum Field implements ImplField {
        CREATION_TIME("interval.start"),
        GATEWAY("gateway"),
        ORIGIN("origin"),
        INTERVAL("interval"),
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
    public Device getGateway() {
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