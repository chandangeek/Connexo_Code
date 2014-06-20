package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

final class ChannelValidationImpl implements ChannelValidation {

    private long id;
    private Reference<Channel> channel = ValueReference.absent();
    private Reference<MeterActivationValidation> meterActivationValidation = ValueReference.absent();
    private UtcInstant lastChecked;

    private final DataModel dataModel;
    private final MeteringService meteringService;

    @SuppressWarnings("unused")
    @Inject
    ChannelValidationImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    ChannelValidationImpl init(MeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        this.meterActivationValidation.set(meterActivationValidation);
        this.channel.set(channel);
        return this;
    }

    static ChannelValidationImpl from(DataModel dataModel, MeterActivationValidation meterActivationValidation, Channel channel) {
        return dataModel.getInstance(ChannelValidationImpl.class).init(meterActivationValidation, channel);
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public MeterActivationValidation getMeterActivationValidation() {
        return meterActivationValidation.get();
    }

    @Override
    public Date getLastChecked() {
        return lastChecked != null ? lastChecked.toDate() : null;
    }

    @Override
    public void setLastChecked(Date date) {
        lastChecked = new UtcInstant(date);
    }

    public Channel getChannel() {
        return channel.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (id > 0) {
            return id == ((ChannelValidationImpl) o).id;
        } else {
            return channel.equals(((ChannelValidationImpl) o).channel) && meterActivationValidation.equals(((ChannelValidationImpl) o).meterActivationValidation);
        }

    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, meterActivationValidation);
    }
}
