package com.elster.jupiter.validation.impl;

import java.time.Instant;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface IMeterActivationValidation extends MeterActivationValidation {

    ChannelValidation addChannelValidation(Channel channel);

    void setRuleSet(ValidationRuleSet ruleSet);

    void makeObsolete();

    void validate();

    void validate(String readingTypeCode);

    void updateLastChecked(Instant lastChecked);
    
    void moveLastCheckedBefore(Instant instant);

    boolean isAllDataValidated();

    Instant getMinLastChecked();

    Instant getMaxLastChecked();
}
