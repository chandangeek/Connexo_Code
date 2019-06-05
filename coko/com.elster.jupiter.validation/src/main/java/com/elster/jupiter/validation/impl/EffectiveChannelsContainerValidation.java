/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;


import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class EffectiveChannelsContainerValidation implements ChannelsContainerValidation {

    private ChannelsContainerValidation channelsContainerValidation;
    private RangeSet<Instant> ranges;

    public EffectiveChannelsContainerValidation(ChannelsContainerValidation channelsContainerValidation, RangeSet<Instant> ranges) {
        this.channelsContainerValidation = channelsContainerValidation;
        this.ranges = ranges;
    }

    public ChannelsContainerValidation getChannelsContainerValidation() {
        return channelsContainerValidation;
    }

    public RangeSet<Instant> getRanges() {
        return ranges;
    }

    @Override
    public long getId() {
        return channelsContainerValidation.getId();
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        return channelsContainerValidation.getRuleSet();
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return channelsContainerValidation.getChannelsContainer();
    }

    @Override
    public void save() {
        channelsContainerValidation.save();
    }

    @Override
    public boolean isObsolete() {
        return channelsContainerValidation.isObsolete();
    }

    @Override
    public Set<ChannelValidation> getChannelValidations() {
        return channelsContainerValidation.getChannelValidations();
    }

    @Override
    public Instant getLastRun() {
        return channelsContainerValidation.getLastRun();
    }

    @Override
    public boolean isActive() {
        return channelsContainerValidation.isActive();
    }

    @Override
    public void activate() {
        channelsContainerValidation.activate();
    }

    @Override
    public void setInitialActivationStatus(boolean status) {
        channelsContainerValidation.setInitialActivationStatus(status);
    }

    @Override
    public void deactivate() {
        channelsContainerValidation.deactivate();
    }

    @Override
    public Optional<ChannelValidation> getChannelValidation(Channel channel) {
        return channelsContainerValidation.getChannelValidation(channel);
    }

    @Override
    public ChannelValidation addChannelValidation(Channel channel) {
        return channelsContainerValidation.addChannelValidation(channel);
    }

    @Override
    public void setRuleSet(ValidationRuleSet ruleSet) {
        channelsContainerValidation.setRuleSet(ruleSet);
    }

    @Override
    public void makeObsolete() {
        channelsContainerValidation.makeObsolete();
    }

    @Override
    public void validate(Collection<Channel> channels, Logger logger) {
        channelsContainerValidation.validate(channels, ranges, logger);
    }

    @Override
    public void validate(Collection<Channel> channels, Instant until, Logger logger) {
        channelsContainerValidation.validate(channels, ranges.subRangeSet(Range.atMost(until)), logger);
    }

    @Override
    public void validate(RangeSet<Instant> ranges, Logger logger) {
        channelsContainerValidation.validate(ranges, logger);
    }

    @Override
    public void validate(Collection<Channel> channels, RangeSet<Instant> ranges, Logger logger) {
        channelsContainerValidation.validate(channels, ranges, logger);
    }

    @Override
    public void updateLastChecked(Instant lastChecked) {
        channelsContainerValidation.updateLastChecked(lastChecked);
    }

    @Override
    public void moveLastCheckedBefore(Map<Long, Range<Instant>> rangeByChannelIdMap) {
        channelsContainerValidation.moveLastCheckedBefore(rangeByChannelIdMap);
    }

    @Override
    public void moveLastCheckedBefore(Instant date) {
        channelsContainerValidation.moveLastCheckedBefore(date);
    }

    @Override
    public boolean isAllDataValidated() {
        return channelsContainerValidation.isAllDataValidated();
    }

    @Override
    public Instant getMinLastChecked() {
        return channelsContainerValidation.getMinLastChecked();
    }

    @Override
    public Instant getMaxLastChecked() {
        return channelsContainerValidation.getMaxLastChecked();
    }
}
