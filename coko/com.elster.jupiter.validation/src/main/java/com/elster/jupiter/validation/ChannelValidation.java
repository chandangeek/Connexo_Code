package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;

import java.time.Instant;

public interface ChannelValidation {

    long getId();

    MeterActivationValidation getMeterActivationValidation();

    Instant getLastChecked();

    Channel getChannel();

    boolean hasActiveRules();
}
