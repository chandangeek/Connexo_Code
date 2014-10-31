package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

final class ChannelValidationImpl implements IChannelValidation {

    private long id;
    private Reference<Channel> channel = ValueReference.absent();
    private Reference<IMeterActivationValidation> meterActivationValidation = ValueReference.absent();
    private Instant lastChecked;
    private boolean activeRules;

    @Inject
    ChannelValidationImpl() {
    }

    ChannelValidationImpl init(IMeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        this.meterActivationValidation.set(meterActivationValidation);
        this.channel.set(channel);
        this.lastChecked = minLastChecked();
        this.activeRules = true;
        return this;
    }

    static ChannelValidationImpl from(DataModel dataModel, IMeterActivationValidation meterActivationValidation, Channel channel) {
        return dataModel.getInstance(ChannelValidationImpl.class).init(meterActivationValidation, channel);
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public IMeterActivationValidation getMeterActivationValidation() {
        return meterActivationValidation.get();
    }

    @Override
    public Instant getLastChecked() {
        return lastChecked;
    }

    public Channel getChannel() {
        return channel.get();
    }

    @Override
    public boolean hasActiveRules() {
        return !activeRules().isEmpty();
    }

    private List<IValidationRule> activeRules() {
    	return getMeterActivationValidation().getRuleSet().getRules().stream()
    			.filter(this::isApplicable)
    			.map(IValidationRule.class::cast)
                .collect(Collectors.toList());
    }
    
    private boolean isApplicable(ValidationRule rule) {
    	return rule.isActive() && rule.getReadingTypes().stream().anyMatch(readingType -> channel.get().getReadingTypes().contains(readingType));
    }
    
    void setActiveRules(boolean activeRules) {
        this.activeRules = activeRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return channel.equals(((ChannelValidationImpl) o).channel) && meterActivationValidation.equals(((ChannelValidationImpl) o).meterActivationValidation);

    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, meterActivationValidation);
    }
    
    private final Instant minLastChecked() {
    	return meterActivationValidation.get().getMeterActivation().getStart();
    }
    
    @Override
    public void updateLastChecked(Instant instant) {
    	Instant newValue = Objects.requireNonNull(instant).isBefore(minLastChecked()) ? minLastChecked() : instant;
    	if (lastChecked.isAfter(newValue)) {
    		channel.get().findReadingQuality(Range.greaterThan(newValue)).stream()
    			.filter(this::isRelevant)
    			.forEach(ReadingQualityRecord::delete);
    	}
    	this.lastChecked = newValue;
    }
    
    @Override
    public void moveLastCheckedBefore(Instant instant) {
    	if (!lastChecked.isAfter(instant)) {
    		return;
    	}
    	Optional<BaseReadingRecord> reading = channel.get().getReadingsBefore(instant, 1).stream().findFirst();
    	updateLastChecked(reading.map(BaseReading::getTimeStamp).orElseGet(this::minLastChecked));
    }
    
    private boolean isRelevant(ReadingQualityRecord readingQuality) {
    	return readingQuality.hasReasonabilityCategory() || readingQuality.hasValidationCategory(); 
    }
    
    @Override
    public void validate() {
    	Instant end = channel.get().getLastDateTime();
    	if (end == null || !lastChecked.isBefore(end)) {
    		return;
    	}
    	Range<Instant> range = Range.openClosed(lastChecked, end);
    	Instant newLastChecked = activeRules().stream()    			
    			.map(validationRule -> validationRule.validateChannel(channel.get(), range))    			
    			.min(Comparator.naturalOrder())
    			.orElse(end);
    	updateLastChecked(newLastChecked);
    }
    	        
}
