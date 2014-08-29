package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.Date;

public interface IMeterActivationValidation extends MeterActivationValidation {

    ChannelValidation addChannelValidation(Channel channel);

    void setRuleSet(ValidationRuleSet ruleSet);

    void makeObsolete();

    void validate(Interval interval);

    void updateLastChecked(Date lastChecked);

    boolean isAllDataValidated();

    Date getMinLastChecked();
}
