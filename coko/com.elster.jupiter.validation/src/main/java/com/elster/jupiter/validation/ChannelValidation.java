package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;

import java.util.Date;

public interface ChannelValidation {

    long getId();

    MeterActivationValidation getMeterActivationValidation();

    Date getLastChecked();

    Channel getChannel();
}
