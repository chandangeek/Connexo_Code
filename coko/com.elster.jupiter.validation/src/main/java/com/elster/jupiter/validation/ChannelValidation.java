package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.validation.impl.IMeterActivationValidation;

import java.util.Date;

public interface ChannelValidation {

    long getId();

    IMeterActivationValidation getMeterActivationValidation();

    Date getLastChecked();

    void setLastChecked(Date date);

    Channel getChannel();
}
