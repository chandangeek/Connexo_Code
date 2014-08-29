package com.elster.jupiter.validation;


import com.elster.jupiter.metering.MeterActivation;

import java.util.Date;
import java.util.Set;

public interface MeterActivationValidation {

    ValidationRuleSet getRuleSet();

    long getId();

    MeterActivation getMeterActivation();

    void save();

    boolean isObsolete();

    Set<ChannelValidation> getChannelValidations();

    Date getLastRun();

    boolean isActive();

    void activate();

    void deactivate();
}
