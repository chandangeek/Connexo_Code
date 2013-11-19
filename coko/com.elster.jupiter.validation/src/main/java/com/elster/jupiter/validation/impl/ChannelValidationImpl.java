package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;
import java.util.Objects;

final class ChannelValidationImpl implements ChannelValidation {

    private long id;
    private Channel channel;
    private long meterActivationValidationId;
    private transient MeterActivationValidation meterActivationValidation;
    private UtcInstant lastChecked;

    @SuppressWarnings("unused")
    private ChannelValidationImpl() {
    }

    ChannelValidationImpl(MeterActivationValidationImpl meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        id = channel.getId();
        this.meterActivationValidation = meterActivationValidation;
        meterActivationValidationId = meterActivationValidation.getId();
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public MeterActivationValidation getMeterActivationValidation() {
        if (meterActivationValidation == null) {
            meterActivationValidation = Bus.getOrmClient().getMeterActivationValidationFactory().get(meterActivationValidationId).get();
        }
        return meterActivationValidation;
    }

    @Override
    public Date getLastChecked() {
        return lastChecked.toDate();
    }

    @Override
    public void setLastChecked(Date date) {
        lastChecked = new UtcInstant(date);
    }

    public Channel getChannel() {
        if (channel == null) {
            channel = Bus.getMeteringService().findChannel(id).get();
        }
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((ChannelValidationImpl) o).id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
