package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.Date;
import java.util.Set;

public interface MeterActivationValidation {

    ChannelValidation addChannelValidation(Channel channel);

    void setRuleSet(ValidationRuleSet ruleSet);

    ValidationRuleSet getRuleSet();

    long getId();

    MeterActivation getMeterActivation();

    void save();

    Set<ChannelValidation> getChannelValidations();

    void validate(Interval interval);

    Date getLastRun();
}
