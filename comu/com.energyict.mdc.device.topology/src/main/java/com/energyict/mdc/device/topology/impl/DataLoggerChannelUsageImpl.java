/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

public class DataLoggerChannelUsageImpl implements DataLoggerChannelUsage {

    public enum Field implements ImplField {
        PHYSICALGATEWAYREF("physicalGatewayReference"),
        ORIGIN_CHANNEL("slaveChannel"),
        GATEWAY_CHANNEL("dataLoggerChannel")
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

    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<PhysicalGatewayReference> physicalGatewayReference = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Channel> dataLoggerChannel = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Channel> slaveChannel = ValueReference.absent();

    public DataLoggerChannelUsageImpl createFor(PhysicalGatewayReference physicalGatewayReference, Channel slaveChannel, Channel dataLoggerChannel ) {
        this.physicalGatewayReference.set(physicalGatewayReference);
        this.slaveChannel.set(slaveChannel);
        this.dataLoggerChannel.set(dataLoggerChannel);
        return this;
    }

    @Override
    public PhysicalGatewayReference getPhysicalGatewayReference() {
        return this.physicalGatewayReference.get();
    }

    @Override
    public Channel getDataLoggerChannel() {
        return dataLoggerChannel.get();
    }

    @Override
    public Channel getSlaveChannel() {
        return slaveChannel.get();
    }

    @Override
    public Interval getInterval() {
        return this.physicalGatewayReference.get().getInterval();
    }

    @Override
    public Range<Instant> getRange() {
        return this.physicalGatewayReference.get().getRange();
    }

    @Override
    public boolean isEffectiveAt(Instant instant) {
        return this.physicalGatewayReference.get().isEffectiveAt(instant);
    }

    @Override
    public boolean overlaps(Range<Instant> otherRange) {
        return this.physicalGatewayReference.get().overlaps(otherRange);
    }

    @Override
    public Optional<Range<Instant>> intersection(Range<Instant> otherRange) {
        return this.physicalGatewayReference.get().intersection(otherRange);
    }
}
