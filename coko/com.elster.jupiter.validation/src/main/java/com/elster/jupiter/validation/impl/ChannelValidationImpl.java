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
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.google.common.collect.Range;

final class ChannelValidationImpl implements IChannelValidation {

    private long id;
    private long channelId;
    private Reference<IMeterActivationValidation> meterActivationValidation = ValueReference.absent();
    private Instant lastChecked;
    @SuppressWarnings("unused")
	private boolean activeRules;
    private Channel channel;

    @Inject
    ChannelValidationImpl() {
    }

    ChannelValidationImpl init(IMeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        this.meterActivationValidation.set(meterActivationValidation);
        this.channelId = channel.getId();
        this.channel = channel;
        this.lastChecked = minLastChecked();
        this.activeRules = true;
        return this;
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
    	if (channel == null) {
    		channel = meterActivationValidation.get().getChannels().stream()
        		.filter(channel -> channel.getId() == channelId)
        		.findFirst()
        		.get();
    	}
    	return channel;
    }

    @Override
    public boolean hasActiveRules() {
        return !activeRules().isEmpty();
    }

    private List<IValidationRule> activeRules() {
    	return getMeterActivationValidation().getRuleSet().getRules().stream()
    			.map(IValidationRule.class::cast)
    			.filter(rule -> rule.appliesTo(getChannel()))    			
                .collect(Collectors.toList());
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
        return channelId == ((ChannelValidationImpl) o).channelId && meterActivationValidation.equals(((ChannelValidationImpl) o).meterActivationValidation);

    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, meterActivationValidation);
    }
    
    private final Instant minLastChecked() {
    	return meterActivationValidation.get().getMeterActivation().getStart();
    }
    
    @Override
    public boolean updateLastChecked(Instant instant) {
    	if (lastChecked.equals(Objects.requireNonNull(instant))) {
    		return false;
    	}
    	Instant newValue = Objects.requireNonNull(instant).isBefore(minLastChecked()) ? minLastChecked() : instant;
    	if (lastChecked.isAfter(newValue)) {
    		getChannel().findReadingQuality(Range.greaterThan(newValue)).stream()
    			.filter(this::isRelevant)
    			.forEach(ReadingQualityRecord::delete);
    	}
    	this.lastChecked = newValue;
    	return true;
    }
    
    @Override
    public boolean moveLastCheckedBefore(Instant instant) {
    	if (!lastChecked.isAfter(instant)) {
    		return false;
    	}
    	Optional<BaseReadingRecord> reading = getChannel().getReadingsBefore(instant, 1).stream().findFirst();
    	return updateLastChecked(reading.map(BaseReading::getTimeStamp).orElseGet(this::minLastChecked));    	
    }
    
    private boolean isRelevant(ReadingQualityRecord readingQuality) {
    	return readingQuality.hasReasonabilityCategory() || readingQuality.hasValidationCategory(); 
    }
    
    @Override
    public void validate() {
    	Instant end = getChannel().getLastDateTime();
    	if (end == null || !lastChecked.isBefore(end)) {
    		return;
    	}
    	Range<Instant> range = Range.openClosed(lastChecked, end);
    	ChannelValidator validator = new ChannelValidator(getChannel(), range) ;
    	Instant newLastChecked = activeRules().stream()    	    	
    			.map(validator::validateRule)  			
    			.min(Comparator.naturalOrder())
    			.orElse(end);
    	updateLastChecked(newLastChecked);
    }
    	        
}
