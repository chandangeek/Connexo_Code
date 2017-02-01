/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages validation for the channels container by concrete rule set.
 */
public interface ChannelsContainerValidation extends HasId {

    ValidationRuleSet getRuleSet();

    ChannelsContainer getChannelsContainer();

    void save();

    boolean isObsolete();

    Set<ChannelValidation> getChannelValidations();

    Instant getLastRun();

    boolean isActive();

    void activate();

    void deactivate();

    Optional<ChannelValidation> getChannelValidation(Channel channel);

    ChannelValidation addChannelValidation(Channel channel);

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
    void moveLastCheckedBefore(Map<Channel, Range<Instant>> ranges);

    void moveLastCheckedBefore(Instant date);

    boolean isAllDataValidated();

    Instant getMinLastChecked();

    Instant getMaxLastChecked();
}
