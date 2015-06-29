package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    Optional<IChannelValidation> getChannelValidation(Channel channel);
    IChannelValidation addChannelValidation(Channel channel);
    void setRuleSet(ValidationRuleSet ruleSet);
    void makeObsolete();
    void validate();
    void validate(ReadingType readingType);
    void updateLastChecked(Instant lastChecked);

    /**
     * Only updates the lastChecked in memory !!! for performance optimisation COPL-882
     *
     * @param ranges: Map of channel-range to update the last checked to
     */
    void moveLastCheckedBefore(Map<Channel,Range<Instant>> ranges);
    boolean isAllDataValidated();
    Instant getMinLastChecked();

    Instant getMaxLastChecked();
    
    List<? extends Channel> getChannels();
}
