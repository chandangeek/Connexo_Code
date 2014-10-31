package com.elster.jupiter.validation;


import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface MeterActivationValidation {

    ValidationRuleSet getRuleSet();

    long getId();

    MeterActivation getMeterActivation();

    void save();

    boolean isObsolete();

    Set<ChannelValidation> getChannelValidations();

    Instant getLastRun();

    boolean isActive();

    void activate();

    void deactivate();

    Optional<? extends ChannelValidation> getChannelValidation(Channel channel);
}
