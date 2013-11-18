package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.UtcInstant;

class ChannelValidation {

    private long id;
    private Channel channel;
    private long meterActivationValidationId;
    private transient MeterActivationValidation meterActivationValidation;
    private UtcInstant lastChecked;

    @SuppressWarnings("unused")
    private ChannelValidation() {
        // for persistence
    }

    ChannelValidation(MeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        id = channel.getId();
        meterActivationValidationId = meterActivationValidation.getId();
    }


}
