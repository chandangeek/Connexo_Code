/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
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

    void validate(Collection<Channel> channels);

    void updateLastChecked(Instant lastChecked);

    /**
     * Only updates the lastChecked in memory!!! For performance optimization COPL-882.
     *
     * @param rangeByChannelIdMap: Map of channelId-range to move the last checked before.
     * Channel must be identified by id here because there can be {@link AggregatedChannel}
     * that is just a wrapping on {@link Channel} with the same id.
     */
    void moveLastCheckedBefore(Map<Long, Range<Instant>> rangeByChannelIdMap);

    void moveLastCheckedBefore(Instant date);

    boolean isAllDataValidated();

    Instant getMinLastChecked();

    Instant getMaxLastChecked();
}
