package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
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

class MeterActivationValidationImpl implements IMeterActivationValidation {

    private long id;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private Reference<IValidationRuleSet> ruleSet = ValueReference.absent();
    private Instant lastRun;
    private List<IChannelValidation> channelValidations = new ArrayList<>();
    private Instant obsoleteTime;

    private final DataModel dataModel;
    private final Clock clock;
    private boolean active = true;

    @Inject
    MeterActivationValidationImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    MeterActivationValidationImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        return this;
    }

    @Override
    public MeterActivation getMeterActivation() {
        return meterActivation.get();
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
        Condition condition = Where.where("channel").isEqualTo(channel).and(Where.where("meterActivationValidation.obsoleteTime").isNull());
        dataModel.query(IChannelValidation.class,  IMeterActivationValidation.class).select(condition).stream()
        	.map(IChannelValidation::getLastChecked)
        	.min(Comparator.naturalOrder())
        	.filter(lastChecked -> lastChecked.isAfter(channelValidation.getLastChecked()))
        	.ifPresent(lastChecked -> channelValidation.updateLastChecked(lastChecked));        
        channelValidations.add(channelValidation);
        return channelValidation;
    }

    @Override
    public Optional<IChannelValidation> getChannelValidation(Channel channel) {
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
            dataModel.mapper(IChannelValidation.class).update(channelValidations);
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
    public Set<IChannelValidation> getChannelValidations() {
        return Collections.unmodifiableSet(new HashSet<>(channelValidations));
    }
    
    @Override
    public void validate() {
    	if (!isActive()) {
    		return;
    	}
    	getMeterActivation().getChannels().forEach(this::validateChannel);
        lastRun = Instant.now(clock);
        save();
    }

    @Override
    public void validate(ReadingType readingType) {
        if (!isActive()) {
            return;
        }
        getMeterActivation().getChannels().stream()
                .filter(channel -> channel.hasReadingType(readingType))
                .forEach(channel -> validateChannel(channel));
        save();
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
            .map(IChannelValidation.class::cast)
            .filter(channelValidation -> channelValidation.updateLastChecked(lastChecked))            
            .count();
        if (updateCount > 0) {
        	save();
        }
    }

    @Override
    public boolean isAllDataValidated() {
        if (isActive()) {
            if (lastRun == null) {
                return false;
            }
            Comparator<? super Instant> comparator = nullsLast(naturalOrder());
            return channelValidations.stream()
                    .noneMatch(c -> c.hasActiveRules() && comparator.compare(c.getLastChecked(), c.getChannel().getLastDateTime()) < 0);
        }
        return channelValidations.stream()
                .noneMatch(IChannelValidation::hasActiveRules);
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
                .filter(IChannelValidation::hasActiveRules)
                .map(IChannelValidation::getLastChecked)
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
        setActive(true);
        getMeterActivation().getChannels().stream()
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
    public List<? extends Channel> getChannels() {
    	return meterActivation.get().getChannels();
    }
}
