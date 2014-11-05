package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface IMeterActivationValidation  {
	ValidationRuleSet getRuleSet();
    long getId();
    MeterActivation getMeterActivation();
    void save();
    boolean isObsolete();
    Set<IChannelValidation> getChannelValidations();
    Instant getLastRun();
    boolean isActive();
    void activate();
    void deactivate();
    Optional<? extends IChannelValidation> getChannelValidation(Channel channel);
    IChannelValidation addChannelValidation(Channel channel);
    void setRuleSet(ValidationRuleSet ruleSet);
    void makeObsolete();
    void validate();
    void validate(ReadingType readingType);
    void updateLastChecked(Instant lastChecked);
    void moveLastCheckedBefore(Instant instant);
    boolean isAllDataValidated();
    Instant getMinLastChecked();

    Instant getMaxLastChecked();
    
    List<? extends Channel> getChannels();
}
