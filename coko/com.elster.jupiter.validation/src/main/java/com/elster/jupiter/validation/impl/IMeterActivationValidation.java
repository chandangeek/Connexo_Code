package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.Range;

import java.time.Instant;

public interface IMeterActivationValidation extends MeterActivationValidation {

    ChannelValidation addChannelValidation(Channel channel);

    void setRuleSet(ValidationRuleSet ruleSet);

    void makeObsolete();

    void validate();

    void validate(Range<Instant> interval, String readingTypeCode);

    void updateLastChecked(Instant lastChecked);

    boolean isAllDataValidated();

    Instant getMinLastChecked();

    Instant getMaxLastChecked();
}
