package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ChannelValidation;

import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

final class ChannelValidationImpl implements ChannelValidation {

    private long id;
    private Reference<Channel> channel = ValueReference.absent();
    private Reference<IMeterActivationValidation> meterActivationValidation = ValueReference.absent();
    private UtcInstant lastChecked;

    @SuppressWarnings("unused")
    @Inject
    ChannelValidationImpl() {
    }

    ChannelValidationImpl init(IMeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        this.meterActivationValidation.set(meterActivationValidation);
        this.channel.set(channel);
        return this;
    }

    static ChannelValidationImpl from(DataModel dataModel, IMeterActivationValidation meterActivationValidation, Channel channel) {
        return dataModel.getInstance(ChannelValidationImpl.class).init(meterActivationValidation, channel);
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public IMeterActivationValidation getMeterActivationValidation() {
        return meterActivationValidation.get();
    }

    @Override
    public Date getLastChecked() {
        return lastChecked != null ? lastChecked.toDate() : null;
    }

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
        return channel.equals(((ChannelValidationImpl) o).channel) && meterActivationValidation.equals(((ChannelValidationImpl) o).meterActivationValidation);

    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, meterActivationValidation);
    }
}
