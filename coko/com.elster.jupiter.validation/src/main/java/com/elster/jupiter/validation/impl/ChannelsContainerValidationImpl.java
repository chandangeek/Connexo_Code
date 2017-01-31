/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

class ChannelsContainerValidationImpl implements ChannelsContainerValidation {

    private long id;
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();
    private Reference<IValidationRuleSet> ruleSet = ValueReference.absent();
    private Instant lastRun;
    private List<ChannelValidation> channelValidations = new ArrayList<>();
    private Instant obsoleteTime;

    private final DataModel dataModel;
    private final Clock clock;
    private boolean active = true;

    @Inject
    ChannelsContainerValidationImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    ChannelsContainerValidationImpl init(ChannelsContainer channelsContainer) {
        this.channelsContainer.set(channelsContainer);
        return this;
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return channelsContainer.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public IValidationRuleSet getRuleSet() {
        return ruleSet.get();
    }

    @Override
    public void setRuleSet(ValidationRuleSet ruleSet) {
        this.ruleSet.set((IValidationRuleSet) ruleSet);
    }

    @Override
    public ChannelValidationImpl addChannelValidation(Channel channel) {
        ChannelValidationImpl channelValidation = new ChannelValidationImpl().init(this, channel);
        // It is possible that channel is a wrapper on persisted entity (for example AggregatedChannelImpl), so use the channelId!
        Condition condition = Where.where("channelId").isEqualTo(channel.getId()).and(Where.where("channelsContainerValidation.obsoleteTime").isNull());
        dataModel.query(ChannelValidation.class, ChannelsContainerValidation.class).select(condition).stream()
                .map(ChannelValidation::getLastChecked)
                .min(Comparator.naturalOrder())
                .filter(lastChecked -> lastChecked.isAfter(channelValidation.getLastChecked()))
                .ifPresent(channelValidation::updateLastChecked);
        channelValidations.add(channelValidation);
        return channelValidation;
    }

    @Override
    public Optional<ChannelValidation> getChannelValidation(Channel channel) {
        return channelValidations.stream()
                .filter(v -> v.getChannel().getId() == channel.getId())
                .findFirst();
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.persist(this);
        } else {
            dataModel.update(this);
            dataModel.mapper(ChannelValidation.class).update(channelValidations);
        }
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTime = Instant.now(clock);
        this.save();
    }

    @Override
    public boolean isObsolete() {
        return obsoleteTime != null;
    }

    @Override
    public Set<ChannelValidation> getChannelValidations() {
        return Collections.unmodifiableSet(new HashSet<>(channelValidations));
    }

    @Override
    public void validate() {
        if (isActive()) {
            getChannelsContainer().getChannels().forEach(this::validateChannel);
            lastRun = Instant.now(clock);
            save();
        }
    }

    @Override
    public void validate(ReadingType readingType) {
        if (isActive()) {
            getChannelsContainer().getChannels().stream()
                    .filter(channel -> channel.hasReadingType(readingType))
                    .forEach(this::validateChannel);
            save();
        }
    }

    private void validateChannel(Channel channel) {
        List<IValidationRule> activeRules = getActiveRules();
        if (hasApplicableRules(channel, activeRules)) {
            ChannelValidationImpl channelValidation = findOrAddValidationFor(channel);
            channelValidation.validate();
            channelValidation.setActiveRules(true);
        } else {
            ChannelValidationImpl channelValidation = findValidationFor(channel);
            if (channelValidation != null) {
                channelValidation.setActiveRules(false);
            }
        }
    }

    private boolean hasApplicableRules(Channel channel, List<IValidationRule> activeRules) {
        return activeRules.stream()
                .anyMatch(r -> isApplicable(r, channel));
    }

    private boolean isApplicable(IValidationRule activeRule, Channel channel) {
        Set<ReadingType> activeRuleReadingTypes = activeRule.getReadingTypes();
        for (ReadingType readingType : channel.getReadingTypes()) {
            if (activeRuleReadingTypes.contains(readingType)) {
                return true;
            }
        }
        return false;
    }

    private List<IValidationRule> getActiveRules() {
        return getRuleSet().getRules().stream()
                .filter(IValidationRule::isActive)
                .collect(Collectors.toList());
    }

    private ChannelValidationImpl findOrAddValidationFor(final Channel channel) {
        ChannelValidationImpl channelValidation = findValidationFor(channel);
        return channelValidation == null ? addChannelValidation(channel) : channelValidation;
    }

    private ChannelValidationImpl findValidationFor(final Channel channel) {
        return channelValidations.stream()
                .filter(c -> c.getChannel().getId() == channel.getId())
                .map(ChannelValidationImpl.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateLastChecked(Instant lastChecked) {
        long updateCount = channelValidations.stream()
                .filter(channelValidation -> channelValidation.updateLastChecked(lastChecked))
                .count();
        if (updateCount > 0) {
            save();
        }
    }

    @Override
    public boolean isAllDataValidated() {
        if (channelValidations.isEmpty() || (lastRun == null && !getChannelsContainer().getChannels().parallelStream().anyMatch(Channel::hasData))) {
            return false;
        }
        Comparator<? super Instant> comparator = nullsLast(naturalOrder());
        return channelValidations.stream()
                .filter(channelValidation -> channelValidation.getChannel().getLastDateTime() != null)
                .anyMatch(c -> c.hasActiveRules() && comparator.compare(c.getLastChecked(), c.getChannel().getLastDateTime()) >= 0);
    }

    @Override
    public Instant getMinLastChecked() {
        return lastCheckedStream()
                .min(naturalOrder())
                .orElse(null);
    }

    @Override
    public Instant getMaxLastChecked() {
        return lastCheckedStream()
                .max(naturalOrder())
                .orElse(null);
    }

    private Stream<Instant> lastCheckedStream() {
        return channelValidations.stream()
                .filter(Objects::nonNull)
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked)
                .filter(Objects::nonNull);
    }

    @Override
    public Instant getLastRun() {
        return lastRun;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        updateLastRun();
        setActive(true);
        getChannelsContainer().getChannels().stream()
                .filter(c -> !getRuleSet().getRules(c.getReadingTypes()).isEmpty())
                .filter(c -> !getChannelValidation(c).isPresent())
                .forEach(this::addChannelValidation);
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    private void setActive(boolean status) {
        this.active = status;
    }

    /**
     * Only updates the lastChecked in memory !!! for performance optimisation COPL-882
     *
     * @param ranges
     */
    @Override
    public void moveLastCheckedBefore(Map<Channel, Range<Instant>> ranges) {
        channelValidations.stream()
                .filter(channelValidation -> ranges.containsKey(channelValidation.getChannel()))
                .forEach(channelValidation -> channelValidation.moveLastCheckedBefore(ranges.get(channelValidation.getChannel()).lowerEndpoint()));
    }

    @Override
    public void moveLastCheckedBefore(Instant date) {
        channelValidations.stream()
                .forEach(channelValidation -> channelValidation.moveLastCheckedBefore(date));
    }

    private void updateLastRun(){
        if(getMinLastChecked() != null) {
            Instant firstMeterActivation = getChannelsContainer().getMeter().flatMap(meter -> {
                List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
                Collections.reverse(meterActivations);
                return Optional.of(meterActivations.get(0).getStart());
            }).orElseGet(() -> null);
            lastRun = getMinLastChecked().equals(firstMeterActivation) ? null : getMinLastChecked();
        }else{
            lastRun = null;
        }
        save();
    }
}
