package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;

import java.util.Date;

interface ChannelValidation {

    long getId();

    MeterActivationValidation getMeterActivationValidation();

    Date getLastChecked();

    void setLastChecked(Date date);

    Channel getChannel();
}
