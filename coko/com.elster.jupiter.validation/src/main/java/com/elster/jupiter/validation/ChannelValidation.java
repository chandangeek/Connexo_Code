package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;

import java.util.Date;

public interface ChannelValidation {

    long getId();

    MeterActivationValidation getMeterActivationValidation();

    Date getLastChecked();

    void setLastChecked(Date date);

    Channel getChannel();
}
