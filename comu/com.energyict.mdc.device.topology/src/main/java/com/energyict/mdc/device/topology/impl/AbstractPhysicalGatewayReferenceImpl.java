/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public abstract class AbstractPhysicalGatewayReferenceImpl implements PhysicalGatewayReference {

    public enum Field implements ImplField {
        CREATION_TIME("interval.start"),
        ORIGIN("origin"),
        INTERVAL("interval"),
        GATEWAY("gateway")
        ;

        private final String javaFieldName;

        Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }
    }

    public static final Map<String, Class<? extends PhysicalGatewayReference>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends PhysicalGatewayReference>>of(
                    "" + PhysicalGatewayReferenceDiscriminator.DEFAULT.ordinal(), PhysicalGatewayReferenceImpl.class,
                    "" + PhysicalGatewayReferenceDiscriminator.DATA_LOGGER_REFERENCE.ordinal(), DataLoggerReferenceImpl.class);

    private long id;
    private Reference<Device> origin = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> gateway = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Interval interval;

    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    public AbstractPhysicalGatewayReferenceImpl createFor(Device origin, Device master, Interval interval) {
        this.interval = interval;
        this.origin.set(origin);
        this.gateway.set(master);
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