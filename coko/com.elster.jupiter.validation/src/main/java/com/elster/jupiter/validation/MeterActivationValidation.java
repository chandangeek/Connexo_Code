package com.elster.jupiter.validation;


import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.Set;

public interface MeterActivationValidation {

    ValidationRuleSet getRuleSet();

     long getId();

    MeterActivation getMeterActivation();

    void save();

    boolean isObsolete();

    Set<ChannelValidation> getChannelValidations();

    void validate(Interval interval);

    Date getLastRun();

    boolean isActive();

    void setActive(boolean status);
}
