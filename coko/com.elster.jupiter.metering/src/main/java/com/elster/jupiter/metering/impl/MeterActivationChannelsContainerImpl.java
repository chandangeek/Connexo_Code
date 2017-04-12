/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

class MeterActivationChannelsContainerImpl extends ChannelsContainerImpl {
    private Reference<MeterActivation> meterActivation = ValueReference.absent();

    @Inject
    MeterActivationChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelBuilder> channelBuilder) {
        super(meteringService, eventService, channelBuilder);
    }

    public MeterActivationChannelsContainerImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        return this;
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return this.meterActivation.get().getMultiplier(type);
    }

    @Override
    public Optional<Meter> getMeter() {
        return meterActivation.flatMap(MeterActivation::getMeter);
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        MeterActivation meterActivation = this.meterActivation.get();
        return meterActivation.isEffectiveAt(instant) ? meterActivation.getMeter() : Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        MeterActivation meterActivation = this.meterActivation.get();
        return meterActivation.isEffectiveAt(instant) ? meterActivation.getUsagePoint() : Optional.empty();
    }

    @Override
    public Interval getInterval() {
        return this.meterActivation.get().getInterval();
    }
}
