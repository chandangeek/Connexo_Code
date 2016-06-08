package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class MeterActivationChannelsContainerImpl extends ChannelsContainerImpl {
    private Reference<MeterActivation> meterActivation = ValueReference.absent();

    @Inject
    public MeterActivationChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelBuilder> channelBuilder) {
        super(meteringService, eventService, channelBuilder);
    }

    public MeterActivationChannelsContainerImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        setInterval(meterActivation.getInterval());
        return this;
    }

    public void save(Provider<MeterActivation> savedMeterActivation) {
        if (getId() == 0) {
            this.meterActivation.setNull();
            getMeteringService().getDataModel().persist(this);
            init(savedMeterActivation.get());
            getMeteringService().getDataModel().update(this);
        } else {
            init(savedMeterActivation.get());
            getMeteringService().getDataModel().update(this);
        }
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return this.meterActivation.get().getMultiplier(type);
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
}
